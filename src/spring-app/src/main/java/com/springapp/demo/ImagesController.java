package com.springapp.demo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class ImagesController {
    public record Result(String Message) {
    }

    // Image represents the image uploaded by the user.
    public record Image(String ImageUUID, LocalDate LastModified) {
        // NewImage creates a new image.
        public static Image NewImage() {
            // Generate a new UUID for the image.
            var id = UUID.randomUUID().toString();

            // Simulate the last modified date.
            var lastModified = LocalDate.now();

            // Create an image with the generated ID and timestamp.
            return new Image(id, lastModified);
        }
    }

    private static Timer s3RequestDuration;
    private static Timer postgresRequestDuration;
    @Autowired
    AmazonS3 s3Client;
    @Autowired
    HikariDataSource datasource;
    @Autowired
    Config.S3Config s3Config;
    private final Tracer tracer;
    public ImagesController(MeterRegistry registry, OpenTelemetry openTelemetry){
        tracer = openTelemetry.getTracer(ImagesController.class.getName(), "0.1.0");
        s3RequestDuration = Timer.builder("myapp_request_duration_seconds").publishPercentiles(0.9, 0.99)
                .tag("op","s3").description("S3 request duration.").register(registry);
        postgresRequestDuration = Timer.builder("myapp_request_duration_seconds").publishPercentiles(0.9, 0.99)
                .tag("op","db").description("Postgres request duration.").register(registry);
    }
    @GetMapping("/api/images")
    public Result getImage() throws IOException, SQLException {
        // Create a new ROOT span to record and trace the request.
        Span parentSpan = tracer.spanBuilder("HTTP GET /api/images").startSpan();
        try (Scope scope = parentSpan.makeCurrent()) {

            // Download the image from S3.
            Date date = download(s3Config.getBucket(), "thumbnail.png");
            // TODO: convertToLocalDateViaInstant(date)
            // Generate a new image.
            var image = Image.NewImage();

            // Save the image ID and the last modified date to the database.
            save(image, "go_image");
        } finally {
            parentSpan.end();
        }
        return new Result("Saved!");
    }

    // Save inserts a newly generated image into the Postgres database.
    private void save(Image image, String table) throws SQLException {
        // Create a new CHILD span to record and trace the request.
        Span span = tracer.spanBuilder("SQL INSERT").startSpan();

        // Get the current time to record the duration of the request.
        long start = System.currentTimeMillis();

        try (Scope scope = span.makeCurrent()) {
            // Prepare the database query to insert a record.
            try (Connection connection = datasource.getConnection();
                 PreparedStatement st = connection.prepareStatement("INSERT INTO " + table + " (id, lastmodified) VALUES (?,?)")) {

                st.setObject(1, UUID.fromString(image.ImageUUID));
                st.setObject(2, image.LastModified);

                st.executeUpdate();

            } catch (SQLException e) {
                throw e;
            }
        } finally {
            span.end();
        }

        long end = System.currentTimeMillis();
        postgresRequestDuration.record(end - start, TimeUnit.MILLISECONDS);
    }

    //download downloads S3 image and returns last modified date.
    private Date download(String bucketName, String key) throws IOException {
        // Create a new CHILD span to record and trace the request.
        Span span = tracer.spanBuilder("S3 GET").startSpan();

        // Get the current time to record the duration of the request.
        long start = System.currentTimeMillis();
        try (Scope scope = span.makeCurrent()) {
            // Prepare the request for the S3 bucket.
            GetObjectRequest requestObject = new GetObjectRequest(bucketName, key);

            // Send the request to the S3 object store to download the image.
            S3Object object = s3Client.getObject(requestObject);

            // Read all the image bytes returned by AWS.
            IOUtils.toByteArray(object.getObjectContent());
            object.close();

            ObjectMetadata objectMetadata = object.getObjectMetadata();
            Date date = objectMetadata.getLastModified();
            if (object != null) {
                object.close();
            }

            // Record the duration of the request to S3.
            long end = System.currentTimeMillis();
            s3RequestDuration.record(end - start, TimeUnit.MILLISECONDS);
            return date;
        } finally {
            span.end();
        }
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
