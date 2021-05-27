package com.akifev.lambda;

import com.akifev.lambda.factory.AwsRdsDataClientFactory;
import com.akifev.lambda.processing.IndexCreator;
import com.akifev.lambda.processing.IndexEjector;
import com.akifev.lambda.repository.FileDescriptionRepository;
import com.akifev.lambda.repository.FileEntryRepository;
import com.amazon.rdsdata.client.RdsDataClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3EventHandler implements RequestHandler<S3Event, String> {

  private static final String PUT_OBJECT_EVENT = "ObjectCreated:Put";

  private static final String REMOVED_OBJECT_EVENT = "ObjectRemoved:Delete";

  private final IndexCreator indexCreator;

  private final IndexEjector indexEjector;

  public S3EventHandler() {

    final RdsDataClient rds = AwsRdsDataClientFactory.createDefault();
    final FileDescriptionRepository fileDescriptionRepository = new FileDescriptionRepository(rds);
    final FileEntryRepository fileEntryRepository = new FileEntryRepository(rds);
    final AmazonS3 s3Client = AmazonS3Client.builder().build();

    this.indexCreator = new IndexCreator(
            fileDescriptionRepository,
            fileEntryRepository,
            s3Client,
            Integer.parseInt(System.getenv("MAX_WORD_LENGTH")),
            Integer.parseInt(System.getenv("CHUNK_SIZE"))
    );
    this.indexEjector = new IndexEjector(fileDescriptionRepository, fileEntryRepository);
  }

  public String handleRequest(S3Event s, Context context) {

    final LambdaLogger logger = context.getLogger();

    for (S3EventNotification.S3EventNotificationRecord record : s.getRecords()) {

      final String eventName = record.getEventName();
      final String bucketName = record.getS3().getBucket().getName();
      final String fileName = record.getS3().getObject().getKey();

      if (eventName.equals(PUT_OBJECT_EVENT)) {

        logger.log(String.format("Creating indexes, file: %s", fileName));
        indexCreator.createIndexes(bucketName, fileName, logger);
        logger.log(String.format("File: %s has been processed", fileName));

      } else if (eventName.equals(REMOVED_OBJECT_EVENT)) {

        logger.log(String.format("Indexes for file: %s", fileName));
        indexEjector.ejectFile(fileName, logger);
        logger.log(String.format("Indexes for file: %s has been removed", fileName));

      } else {
        logger.log(String.format("Unknown event name: %s", eventName));
      }

    }

    return null;
  }

}