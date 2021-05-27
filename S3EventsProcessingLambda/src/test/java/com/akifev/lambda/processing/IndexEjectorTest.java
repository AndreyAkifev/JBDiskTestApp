package com.akifev.lambda.processing;


import com.akifev.lambda.model.FileDescription;
import com.akifev.lambda.model.FileState;
import com.akifev.lambda.repository.FileDescriptionRepository;
import com.akifev.lambda.repository.FileEntryRepository;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IndexEjectorTest {

  private static final String INTERNAL_ERROR_MSG = "Internal error";

  @Mock
  private FileDescriptionRepository fileDescriptionRepository;

  @Mock
  private FileEntryRepository fileEntryRepository;

  @Mock
  private LambdaLogger logger;

  @InjectMocks
  private IndexEjector instance;

  @Test
  public void testEjectFileSuccessful() {

    final String fileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileNameAndState(fileName, FileState.DELETING))
            .thenReturn(testFileDescription);

    instance.ejectFile(fileName, logger);

    verify(fileEntryRepository).deleteAllByFileId(testFileDescription.getId());
    verify(fileDescriptionRepository).deleteById(testFileDescription.getId());
  }

  @Test
  public void testEjectShouldNotBeSuccessfulBecauseFileEntryRepositoryThrewAnException() {

    final String fileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileNameAndState(fileName, FileState.DELETING))
            .thenReturn(testFileDescription);

    doThrow(RuntimeException.class).when(fileEntryRepository).deleteAllByFileId(testFileDescription.getId());

    instance.ejectFile(fileName, logger);

    verify(fileDescriptionRepository)
            .updateStateAndErrorCause(testFileDescription.getId(), FileState.ERROR, INTERNAL_ERROR_MSG);
  }

  @Test
  public void testEjectShouldNotBeSuccessfulBecauseFileDescriptionRepositoryThrewAnException() {

    final String fileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileNameAndState(fileName, FileState.DELETING))
            .thenReturn(testFileDescription);

    doThrow(RuntimeException.class).when(fileDescriptionRepository).deleteById(testFileDescription.getId());

    instance.ejectFile(fileName, logger);

    verify(fileDescriptionRepository)
            .updateStateAndErrorCause(testFileDescription.getId(), FileState.ERROR, INTERNAL_ERROR_MSG);
  }
}