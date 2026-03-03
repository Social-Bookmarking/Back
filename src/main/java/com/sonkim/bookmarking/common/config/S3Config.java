package com.sonkim.bookmarking.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${cloud.r2.credentials.access-key}")
    private String r2AccessKey;

    @Value("${cloud.r2.credentials.secret-key}")
    private String r2SecretKey;

    @Value("${cloud.r2.endpoint}")
    private String endpoint;

    @Value("${aws.lambda.access-key}")
    private String lambdaAccessKey;

    @Value("${aws.lambda.scret-key}")
    private String lambdaSecretKey;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(r2AccessKey, r2SecretKey);
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);

        return S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(provider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(r2AccessKey, r2SecretKey);
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);

        return S3Presigner.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(provider)
                .build();
    }

    @Bean
    public LambdaClient lambdaClient() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(lambdaAccessKey, lambdaSecretKey);
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);

        return LambdaClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(provider)
                .build();
    }
}
