package com.akifev.lambda.processing;

import com.akifev.lambda.model.FileDescription;
import com.akifev.lambda.model.FileState;
import com.akifev.lambda.repository.FileDescriptionRepository;
import com.akifev.lambda.repository.FileEntryRepository;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class IndexEjector {

  private final FileDescriptionRepository fileDescriptionRepository;

  private final FileEntryRepository fileEntryRepository;

  public IndexEjector(final FileDescriptionRepository fileDescriptionRepository,
                      final FileEntryRepository fileEntryRepository) {

    this.fileDescriptionRepository = fileDescriptionRepository;
    this.fileEntryRepository = fileEntryRepository;
  }

  public void ejectFile(final String fileName, final LambdaLogger logger) {

    final FileDescription found = fileDescriptionRepository
            .findByFileNameAndState(fileName, FileState.DELETING);

    try {

      fileEntryRepository.deleteAllByFileId(found.getId());
      fileDescriptionRepository.deleteById(found.getId());
    } catch (Exception e) {

      logger.log(e.getMessage());
      fileDescriptionRepository.updateStateAndErrorCause(
              found.getId(),
              FileState.ERROR,
              "Internal error"
      );
    }
  }
}
