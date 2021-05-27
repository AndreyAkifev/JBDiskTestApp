package com.akifev.jbdisk.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.configuration.aws")
public class AwsProperties {

  private String region;

  private Credentials credentials;

  private S3 s3;

  @Getter
  @Setter
  public static class Credentials {

    private String accessKey;

    private String secretKey;

  }

  @Getter
  @Setter
  public static class S3 {

    private String bucket;

  }


}
