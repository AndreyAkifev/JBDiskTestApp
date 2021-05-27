package com.akifev.lambda.processing;

import com.akifev.lambda.model.FileDescription;
import com.akifev.lambda.model.FileState;
import com.akifev.lambda.repository.FileDescriptionRepository;
import com.akifev.lambda.repository.FileEntryRepository;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectTaggingRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IndexCreatorTest {

  public static final String INTERNAL_ERROR_MSG = "Internal error";
  @Mock
  private AmazonS3 amazonS3;

  @Mock
  private FileDescriptionRepository fileDescriptionRepository;

  @Mock
  private FileEntryRepository fileEntryRepository;

  @Mock
  private LambdaLogger logger;

  @Test
  public void testCreateIndexesOneChunkFileSuccessful() {

    final String bucketName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();
    final String content = "this is a content";

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileName(fileName))
            .thenReturn(testFileDescription);

    final S3Object testS3Object = new S3Object();
    testS3Object.setObjectContent(IOUtils.toInputStream(content, Charset.defaultCharset()));

    when(amazonS3.getObject(bucketName, fileName)).thenReturn(testS3Object);

    new IndexCreator(fileDescriptionRepository, fileEntryRepository, amazonS3, 10, 4)
            .createIndexes(bucketName, fileName, logger);

    final ArgumentCaptor<Collection<String>> wordsCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(fileEntryRepository).createAllIfConflictIgnore(eq(testFileDescription.getId()), wordsCaptor.capture());

    final ArgumentCaptor<DeleteObjectTaggingRequest> deleteObjectTaggingRequestArgumentCaptor
            = ArgumentCaptor.forClass(DeleteObjectTaggingRequest.class);
    verify(amazonS3).deleteObjectTagging(deleteObjectTaggingRequestArgumentCaptor.capture());

    verify(fileDescriptionRepository).updateState(testFileDescription.getId(), FileState.READY);

    final Collection<String> capturedWords = wordsCaptor.getValue();
    assertEquals(4, capturedWords.size());
    assertTrue(capturedWords.contains("this"));
    assertTrue(capturedWords.contains("is"));
    assertTrue(capturedWords.contains("a"));
    assertTrue(capturedWords.contains("content"));

    final DeleteObjectTaggingRequest capturedDeleteObjectTaggingReq
            = deleteObjectTaggingRequestArgumentCaptor.getValue();
    assertEquals(bucketName, capturedDeleteObjectTaggingReq.getBucketName());
    assertEquals(fileName, capturedDeleteObjectTaggingReq.getKey());
  }

  @Test
  public void testCreateIndexesTwoChunksFileSuccessful() {

    final String bucketName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();
    final String content = "this is a content";

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileName(fileName))
            .thenReturn(testFileDescription);

    final S3Object testS3Object = new S3Object();
    testS3Object.setObjectContent(IOUtils.toInputStream(content, Charset.defaultCharset()));

    when(amazonS3.getObject(bucketName, fileName)).thenReturn(testS3Object);

    new IndexCreator(fileDescriptionRepository, fileEntryRepository, amazonS3, 10, 2)
            .createIndexes(bucketName, fileName, logger);

    final ArgumentCaptor<Collection<String>> wordsCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(fileEntryRepository, times(2))
            .createAllIfConflictIgnore(eq(testFileDescription.getId()), wordsCaptor.capture());

    final ArgumentCaptor<DeleteObjectTaggingRequest> deleteObjectTaggingRequestArgumentCaptor
            = ArgumentCaptor.forClass(DeleteObjectTaggingRequest.class);
    verify(amazonS3).deleteObjectTagging(deleteObjectTaggingRequestArgumentCaptor.capture());

    verify(fileDescriptionRepository).updateState(testFileDescription.getId(), FileState.READY);

    final List<Collection<String>> allCapturedChunks = wordsCaptor.getAllValues();
    assertEquals(2, allCapturedChunks.size());
    assertEquals(2, allCapturedChunks.get(0).size());
    assertEquals(2, allCapturedChunks.get(1).size());

    assertTrue(allCapturedChunks.get(0).contains("this"));
    assertTrue(allCapturedChunks.get(0).contains("is"));
    assertTrue(allCapturedChunks.get(1).contains("a"));
    assertTrue(allCapturedChunks.get(1).contains("content"));

    final DeleteObjectTaggingRequest capturedDeleteObjectTaggingReq
            = deleteObjectTaggingRequestArgumentCaptor.getValue();
    assertEquals(bucketName, capturedDeleteObjectTaggingReq.getBucketName());
    assertEquals(fileName, capturedDeleteObjectTaggingReq.getKey());
  }

  @Test
  public void testCreateIndexesTwoChunksAndHalfFileSuccessful() {

    final String bucketName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();
    final String content = "this is a longer content";

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileName(fileName))
            .thenReturn(testFileDescription);

    final S3Object testS3Object = new S3Object();
    testS3Object.setObjectContent(IOUtils.toInputStream(content, Charset.defaultCharset()));

    when(amazonS3.getObject(bucketName, fileName)).thenReturn(testS3Object);

    new IndexCreator(fileDescriptionRepository, fileEntryRepository, amazonS3, 10, 2)
            .createIndexes(bucketName, fileName, logger);

    final ArgumentCaptor<Collection<String>> wordsCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(fileEntryRepository, times(3))
            .createAllIfConflictIgnore(eq(testFileDescription.getId()), wordsCaptor.capture());

    final ArgumentCaptor<DeleteObjectTaggingRequest> deleteObjectTaggingRequestArgumentCaptor
            = ArgumentCaptor.forClass(DeleteObjectTaggingRequest.class);
    verify(amazonS3).deleteObjectTagging(deleteObjectTaggingRequestArgumentCaptor.capture());

    verify(fileDescriptionRepository).updateState(testFileDescription.getId(), FileState.READY);

    final List<Collection<String>> allCapturedChunks = wordsCaptor.getAllValues();
    assertEquals(3, allCapturedChunks.size());
    assertEquals(2, allCapturedChunks.get(0).size());
    assertEquals(2, allCapturedChunks.get(1).size());
    assertEquals(1, allCapturedChunks.get(2).size());

    assertTrue(allCapturedChunks.get(0).contains("this"));
    assertTrue(allCapturedChunks.get(0).contains("is"));
    assertTrue(allCapturedChunks.get(1).contains("a"));
    assertTrue(allCapturedChunks.get(1).contains("longer"));
    assertTrue(allCapturedChunks.get(2).contains("content"));

    final DeleteObjectTaggingRequest capturedDeleteObjectTaggingReq
            = deleteObjectTaggingRequestArgumentCaptor.getValue();
    assertEquals(bucketName, capturedDeleteObjectTaggingReq.getBucketName());
    assertEquals(fileName, capturedDeleteObjectTaggingReq.getKey());
  }

  @Test
  public void testCreateIndexesOnlyCommaSuccessful() {

    final String bucketName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();
    final String content = ",";


    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileName(fileName))
            .thenReturn(testFileDescription);

    final S3Object testS3Object = new S3Object();
    testS3Object.setObjectContent(IOUtils.toInputStream(content, Charset.defaultCharset()));

    when(amazonS3.getObject(bucketName, fileName)).thenReturn(testS3Object);

    new IndexCreator(fileDescriptionRepository, fileEntryRepository, amazonS3, 10, 10)
            .createIndexes(bucketName, fileName, logger);

    verify(fileEntryRepository, times(0))
            .createAllIfConflictIgnore(eq(testFileDescription.getId()), any());

    final ArgumentCaptor<DeleteObjectTaggingRequest> deleteObjectTaggingRequestArgumentCaptor
            = ArgumentCaptor.forClass(DeleteObjectTaggingRequest.class);
    verify(amazonS3).deleteObjectTagging(deleteObjectTaggingRequestArgumentCaptor.capture());

    verify(fileDescriptionRepository).updateState(testFileDescription.getId(), FileState.READY);

    final DeleteObjectTaggingRequest capturedDeleteObjectTaggingReq
            = deleteObjectTaggingRequestArgumentCaptor.getValue();
    assertEquals(bucketName, capturedDeleteObjectTaggingReq.getBucketName());
    assertEquals(fileName, capturedDeleteObjectTaggingReq.getKey());
  }

  @Test
  public void testCreateIndexesTextWithSignsAndSpacesAndBracesSuccessful() {

    final String bucketName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();
    final String content = "ThIs, is!    " + System.lineSeparator() + ",a, coNteNt:)";

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileName(fileName))
            .thenReturn(testFileDescription);

    final S3Object testS3Object = new S3Object();
    testS3Object.setObjectContent(IOUtils.toInputStream(content, Charset.defaultCharset()));

    when(amazonS3.getObject(bucketName, fileName)).thenReturn(testS3Object);

    new IndexCreator(fileDescriptionRepository, fileEntryRepository, amazonS3, 10, 10)
            .createIndexes(bucketName, fileName, logger);

    final ArgumentCaptor<Collection<String>> wordsCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(fileEntryRepository).createAllIfConflictIgnore(eq(testFileDescription.getId()), wordsCaptor.capture());

    final ArgumentCaptor<DeleteObjectTaggingRequest> deleteObjectTaggingRequestArgumentCaptor
            = ArgumentCaptor.forClass(DeleteObjectTaggingRequest.class);
    verify(amazonS3).deleteObjectTagging(deleteObjectTaggingRequestArgumentCaptor.capture());

    final Collection<String> capturedWords = wordsCaptor.getValue();
    assertEquals(4, capturedWords.size());
    assertTrue(capturedWords.contains("this"));
    assertTrue(capturedWords.contains("is"));
    assertTrue(capturedWords.contains("a"));
    assertTrue(capturedWords.contains("content"));

    final DeleteObjectTaggingRequest capturedDeleteObjectTaggingReq
            = deleteObjectTaggingRequestArgumentCaptor.getValue();
    assertEquals(bucketName, capturedDeleteObjectTaggingReq.getBucketName());
    assertEquals(fileName, capturedDeleteObjectTaggingReq.getKey());
  }

  @Test
  public void testCreateIndexesShouldFailBecauseOfLongWord() {
    final String bucketName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();
    final String content = "normal and loooooooong word";

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileName(fileName))
            .thenReturn(testFileDescription);

    final S3Object testS3Object = new S3Object();
    testS3Object.setObjectContent(IOUtils.toInputStream(content, Charset.defaultCharset()));

    when(amazonS3.getObject(bucketName, fileName)).thenReturn(testS3Object);

    new IndexCreator(fileDescriptionRepository, fileEntryRepository, amazonS3, 6, 2)
            .createIndexes(bucketName, fileName, logger);

    final ArgumentCaptor<Collection<String>> wordsCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(fileEntryRepository).createAllIfConflictIgnore(eq(testFileDescription.getId()), wordsCaptor.capture());

    verify(amazonS3, times(0)).deleteObjectTagging(any());

    verify(fileDescriptionRepository, times(0)).updateState(any(), any());

    verify(fileEntryRepository).deleteAllByFileId(testFileDescription.getId());
    verify(fileDescriptionRepository).updateStateAndErrorCause(
            eq(testFileDescription.getId()),
            eq(FileState.ERROR),
            not(eq(INTERNAL_ERROR_MSG))
    );
  }

  @Test
  public void testCreateIndexesShouldFailBecauseOfInternalError() {
    final String bucketName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());

    when(fileDescriptionRepository.findByFileName(fileName))
            .thenReturn(testFileDescription);

    when(amazonS3.getObject(bucketName, fileName)).thenThrow(RuntimeException.class);

    new IndexCreator(fileDescriptionRepository, fileEntryRepository, amazonS3, 6, 2)
            .createIndexes(bucketName, fileName, logger);

    verify(amazonS3, times(0)).deleteObjectTagging(any());

    verify(fileDescriptionRepository, times(0)).updateState(any(), any());

    verify(fileEntryRepository).deleteAllByFileId(testFileDescription.getId());
    verify(fileDescriptionRepository).updateStateAndErrorCause(
            eq(testFileDescription.getId()),
            eq(FileState.ERROR),
            eq(INTERNAL_ERROR_MSG)
    );
  }

}