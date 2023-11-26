package com.springapp.demo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

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

    @Autowired
    AmazonS3 s3Client;
    @Autowired
    HikariDataSource datasource;
    @Autowired
    Config.S3Config s3Config;

    public ImagesController() {
    }

    @GetMapping("/api/images")
    public Result getImage() throws IOException, SQLException {
        // Download the image from S3.
        Date date = download(s3Config.getBucket(), "thumbnail.png");
        // TODO: convertToLocalDateViaInstant(date)
        // Generate a new image.
        var image = Image.NewImage();
        // Save the image ID and the last modified date to the database.
        save(image, "go_image");

        return new Result("Saved!");
    }

    // Save inserts a newly generated image into the Postgres database.
    private void save(Image image, String table) throws SQLException {
        // Prepare the database query to insert a record.
        PreparedStatement st = datasource
                .getConnection()
                .prepareStatement("INSERT INTO " + table + " (id, lastmodified) VALUES (?,?)");
        st.setObject(1, UUID.fromString(image.ImageUUID));
        st.setObject(2, image.LastModified);
        // Execute the query to create a new image record.
        st.executeUpdate();
        st.close();
    }

    //download downloads S3 image and returns last modified date.
    private Date download(String bucketName, String key) throws IOException {
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

        return date;
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
