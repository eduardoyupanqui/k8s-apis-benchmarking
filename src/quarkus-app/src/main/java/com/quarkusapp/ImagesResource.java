package com.quarkusapp;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.event.ObserverException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.postgresql.ds.PGSimpleDataSource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.utils.IoUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
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
    @Named("customS3Client")
    S3Client s3;
    @Inject
    @Named("customAgroalDataSource")
    AgroalDataSource dataSource;
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
        // Prepare the database query to insert a record.
        PreparedStatement st = dataSource
                .getConnection()
                .prepareStatement("INSERT INTO " + table + " (id, lastmodified) VALUES (?,?)");
        st.setObject(1, UUID.fromString(image.ImageUUID));
        st.setObject(2, image.LastModified);
        // Execute the query to create a new image record.
        st.executeUpdate();
        st.close();
    }

    //download downloads S3 image and returns last modified date.
    private Instant download(String bucketName, String key) throws IOException {
        // Prepare the request for the S3 bucket.
        var requestObject = GetObjectRequest.builder()
                .bucket(bucketName)
                .key("thumbnail.png")
                .build();
        // Send the request to the S3 object store to download the image.
        var object = s3.getObject(requestObject);

        // Read all the image bytes returned by AWS.
        IoUtils.toByteArray(object);
        object.close();

        var objectResponse = object.response();
        var date = objectResponse.lastModified();
        if (object != null) {
            object.close();
        }

        return date;
    }
}
