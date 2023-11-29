package com.quarkusapp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@ApplicationScoped
public class S3Configuration {
    private final Config.S3Config s3Config;
    @Inject
    public S3Configuration(Config.S3Config s3Config) {
        this.s3Config = s3Config;
    }
    @Produces()
    @Named("customS3Client")
    public S3Client s3Connect() {
        var credentials = AwsBasicCredentials.create(s3Config.user(),s3Config.secret());
        var awsCredentialsProvider = StaticCredentialsProvider.create(credentials);
        return S3Client
                .builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(s3Config.region()))
                .endpointOverride(URI.create(s3Config.endpoint()))
                .forcePathStyle(s3Config.pathStyle())
                .build();
    }
}
