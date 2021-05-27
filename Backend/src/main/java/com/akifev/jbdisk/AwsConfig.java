package com.akifev.jbdisk;

import com.akifev.jbdisk.properties.AwsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class AwsConfig {

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider(final AwsProperties awsProperties) {

    final AwsProperties.Credentials credentials = awsProperties.getCredentials();
    return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey())
    );
  }

  @Bean
  public S3Client s3Client(final AwsProperties awsProperties,
                           final AwsCredentialsProvider credentialsProvider) {

    final S3ClientBuilder s3ClientBuilder = S3Client.builder();
    s3ClientBuilder.region(Region.of(awsProperties.getRegion()));
    s3ClientBuilder.credentialsProvider(credentialsProvider);

    return s3ClientBuilder.build();
  }

}
