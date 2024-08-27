package com.quarkusapp;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

// Config represents configuration for the app.
public interface Config {
    @ConfigMapping(prefix = "s3")
    public interface S3Config {
        // S3 bucket name to store images.
        @WithName("bucket")String bucket();
    }
}


