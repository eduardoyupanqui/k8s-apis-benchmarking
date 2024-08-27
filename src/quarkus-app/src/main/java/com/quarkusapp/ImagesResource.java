package com.quarkusapp;

import io.agroal.api.AgroalDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Path("/api/images")
public class ImagesResource {
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
    @Inject()
    Config.S3Config s3Config;
    @Inject()
    S3Client s3;
    @Inject
    AgroalDataSource dataSource;
    private final MeterRegistry registry;
    private static Timer s3Timer;
    private static Timer dbTimer;

    public ImagesResource(MeterRegistry registry)
    {
        this.registry = registry;
        s3Timer = Timer.builder("myapp_request_duration_seconds")
                .publishPercentiles(0.9, 0.99)
                .tags("op", "s3")
                .description("S3 request duration.").register(registry);

        dbTimer = Timer.builder("myapp_request_duration_seconds")
                .publishPercentiles(0.9, 0.99)
                .tags("op", "db")
                .description("DB request duration.").register(registry);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result getImage() throws IOException, SQLException {
        // Download the image from S3.
        Instant date = download(s3Config.bucket(), "thumbnail.png");
        // Generate a new image.
        var image = Image.NewImage();
        // Save the image ID and the last modified date to the database.
        save(image, "go_image");

        return new Result("Saved!");
    }

    // Save inserts a newly generated image into the Postgres database.
    private void save(Image image, String table) throws SQLException {
        Timer.Sample sample = Timer.start(registry);

        // Prepare the database query to insert a record.
        try (var con = dataSource.getConnection();
             var st = con.prepareStatement("INSERT INTO " + table + " (id, lastmodified) VALUES (?,?)");){
            st.setObject(1, UUID.fromString(image.ImageUUID));
            st.setObject(2, image.LastModified);
            // Execute the query to create a new image record.
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sample.stop(dbTimer);
    }

    //download downloads S3 image and returns last modified date.
    private Instant download(String bucketName, String key) throws IOException {
        Timer.Sample sample = Timer.start(registry);

        // Prepare the request for the S3 bucket.
        var requestObject = GetObjectRequest.builder()
                .bucket(bucketName)
                .key("thumbnail.png")
                .build();
        // Send the request to the S3 object store to download the image.
        var object = s3.getObjectAsBytes(requestObject);
        // Read all the image bytes returned by AWS.
        var bytes = object.asByteArray();

        sample.stop(s3Timer);

        var objectResponse = object.response();
        return objectResponse.lastModified();
    }
}
