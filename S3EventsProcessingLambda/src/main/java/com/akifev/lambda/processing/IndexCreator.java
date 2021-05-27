package com.akifev.lambda.processing;

import com.akifev.lambda.exception.LambdaException;
import com.akifev.lambda.exception.TooLongWordException;
import com.akifev.lambda.model.FileDescription;
import com.akifev.lambda.model.FileState;
import com.akifev.lambda.repository.FileDescriptionRepository;
import com.akifev.lambda.repository.FileEntryRepository;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectTaggingRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class IndexCreator {

  private static final int LINE_SEPARATOR = 10;

  private final int maxWordLength;

  private final int chunkSze;

  private final AmazonS3 s3Client;

  private final FileDescriptionRepository fileDescriptionRepository;

  private final FileEntryRepository fileEntryRepository;

  public IndexCreator(final FileDescriptionRepository fileDescriptionRepository,
                      final FileEntryRepository fileEntryRepository,
                      final AmazonS3 s3Client,
                      final int maxWordLength,
                      final int chunkSize) {

    this.maxWordLength = maxWordLength;
    this.chunkSze = chunkSize;
    this.fileDescriptionRepository = fileDescriptionRepository;
    this.fileEntryRepository = fileEntryRepository;
    this.s3Client = s3Client;
  }

  public void createIndexes(final String bucketName, final String fileName, final LambdaLogger logger) {

    final FileDescription found = fileDescriptionRepository.findByFileName(fileName);

    try {

      createIndexes(found.getId(), bucketName, fileName);
      s3Client.deleteObjectTagging(new DeleteObjectTaggingRequest(bucketName, fileName));

      fileDescriptionRepository.updateState(found.getId(), FileState.READY);
    } catch (LambdaException e) {

      deleteCreatedEntriesAndSetErrorState(found.getId(), e.getMessage());
    } catch (Exception e) {

      logger.log(e.getMessage());
      deleteCreatedEntriesAndSetErrorState(found.getId(), "Internal error");
    }
  }

  private void deleteCreatedEntriesAndSetErrorState(final Long fileId, final String message) {
    fileEntryRepository.deleteAllByFileId(fileId);
    fileDescriptionRepository.updateStateAndErrorCause(fileId, FileState.ERROR, message);
  }

  private void createIndexes(final Long fileId,
                             final String bucketName,
                             final String fileName) throws TooLongWordException {

    try (final S3ObjectInputStream is = s3Client.getObject(bucketName, fileName).getObjectContent();
         final BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

      StringBuilder sb = new StringBuilder();
      Set<String> words = new HashSet<>(chunkSze);

      int r;

      while ((r = br.read()) != -1) {
        if (Character.isLetter(r)) {
          sb.append(((char) r));
        } else if ((Character.isSpaceChar(r) || r == LINE_SEPARATOR) && sb.length() > 0) {
          words.add(sb.toString().toLowerCase());
          sb.setLength(0);
        }

        if (sb.length() > maxWordLength) {
          throw new TooLongWordException(maxWordLength);
        }

        if (words.size() == chunkSze) {
          fileEntryRepository.createAllIfConflictIgnore(fileId, words);
          words = new HashSet<>();
        }
      }

      if (sb.length() > 0) {
        words.add(sb.toString().toLowerCase());
      }

      if (!words.isEmpty()) {
        fileEntryRepository.createAllIfConflictIgnore(fileId, words);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
