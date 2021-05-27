package com.akifev.jbdisk.storage.impl;

import com.akifev.jbdisk.properties.AwsProperties;
import com.akifev.jbdisk.storage.StorageService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

@Service
public class StorageServiceImpl implements StorageService {

  private static final String EXPIRES_TAG_KEY = "indexing";

  private static final String EXPIRES_TAG_VALUE = "true";

  private final S3Client s3;

  private final String bucketName;

  public StorageServiceImpl(final S3Client s3, final AwsProperties properties) {

    this.s3 = s3;
    this.bucketName = properties.getS3().getBucket();
  }

  @Override
  public void copyIsToFileAndSetExpiresTag(final InputStream is, final String fileName) {

    try {
      final PutObjectRequest putObjectRequest =
              PutObjectRequest.builder()
                      .bucket(bucketName)
                      .tagging(Tagging.builder()
                              .tagSet(
                                      Tag.builder()
                                              .key(EXPIRES_TAG_KEY)
                                              .value(EXPIRES_TAG_VALUE)
                                              .build()
                              ).build())
                      .key(fileName)
                      .build();
      final RequestBody requestBody = RequestBody.fromInputStream(is, is.available());
      s3.putObject(putObjectRequest, requestBody);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public InputStream openIsToFile(final String fileName) {

    final GetObjectRequest getObjectRequest =
            GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName).build();

    return s3.getObject(getObjectRequest);
  }

  @Override
  public void deleteFile(final String fileName) {

    final DeleteObjectRequest deleteObjectRequest =
            DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
    s3.deleteObject(deleteObjectRequest);
  }
}
