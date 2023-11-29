package com.quarkusapp;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

// Config represents configuration for the app.
public interface Config {
    @ConfigMapping(prefix = "s3")
    public interface S3Config {
        // Region for the S3 bucket.
        @WithName("region")String region();
        // S3 bucket name to store images.
        @WithName("bucket")String bucket();
        // S3 endpoint, since we use Minio we must provide
        // a custom endpoint. It should be a DNS of Minio instance.
        @WithName("endpoint")String endpoint();
        // User to access S3 bucket.
        @WithName("user")String user();
        // Secret to access S3 bucket.
        @WithName("secret")String secret();
        // Enable path S3 style; we must enable it to use Minio.
        @WithName("pathStyle")Boolean pathStyle();
    }
    @ConfigMapping(prefix = "db")
    public interface DbConfig {
        // Database to store images.
        @WithName("database")String database();

        // Host to connect database.
        @WithName("host")String host();

        // User to connect database.
        @WithName("user")String user();

        // Password to connect database.
        @WithName("password")String password();
    }
}


