package com.akifev.jbdisk.service.impl;

import com.akifev.jbdisk.exception.FileAlreadyExistsException;
import com.akifev.jbdisk.exception.FileNotFoundException;
import com.akifev.jbdisk.exception.IllegalFileStateException;
import com.akifev.jbdisk.model.FileDescription;
import com.akifev.jbdisk.model.FileState;
import com.akifev.jbdisk.repository.FileDescriptionRepository;
import com.akifev.jbdisk.rest.converter.FileConverter;
import com.akifev.jbdisk.rest.dto.FileDescriptionOutDto;
import com.akifev.jbdisk.storage.StorageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileDescriptionServiceImplTest {

  @Mock
  private FileDescriptionRepository repository;

  @Mock
  private StorageService storageService;

  @Spy
  private FileConverter fileConverter;

  @InjectMocks
  private FileDescriptionServiceImpl instance;

  @Test
  public void testUploadFileSuccessful() {

    final InputStream is = createRandomContentIs();

    final String originalFileName = UUID.randomUUID().toString();

    when(repository.save(Mockito.any(FileDescription.class)))
            .thenAnswer(i -> i.getArgument(0));
    when(repository.existsByOriginalFileName(originalFileName))
            .thenReturn(false);

    final FileDescriptionOutDto result = instance.uploadFile(is, originalFileName);

    final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<FileDescription> fileDescriptionCaptor = ArgumentCaptor.forClass(FileDescription.class);
    verify(storageService).copyIsToFileAndSetExpiresTag(any(), fileNameCaptor.capture());
    verify(repository).save(fileDescriptionCaptor.capture());

    final FileDescription capturedFileDescription = fileDescriptionCaptor.getValue();
    final String savedFileName = capturedFileDescription.getFileName();

    assertEquals(originalFileName, capturedFileDescription.getOriginalFileName());
    assertEquals(FileState.INDEXING, capturedFileDescription.getState());
    assertNull(capturedFileDescription.getErrorCause());
    assertNull(capturedFileDescription.getEntries());
    assertNotEquals(originalFileName, savedFileName);

    assertEquals(savedFileName, fileNameCaptor.getValue());

    assertEquals(originalFileName, result.getOriginalFileName());
    assertEquals(FileState.INDEXING.name(), result.getState());
    assertNull(result.getErrorCause());
  }

  @Test
  public void testUploadShouldFailBecauseOfConflict() {

    final InputStream is = createRandomContentIs();

    final String originalFileName = UUID.randomUUID().toString();

    when(repository.existsByOriginalFileName(originalFileName))
            .thenReturn(true);

    assertThrows(FileAlreadyExistsException.class,
            () -> instance.uploadFile(is, originalFileName)
    );

    verify(storageService, never()).copyIsToFileAndSetExpiresTag(any(), any());
    verify(repository, never()).save(any());
  }

  @Test
  public void testUploadShouldFailBecauseOfStorageException() {

    final InputStream is = createRandomContentIs();

    final String originalFileName = UUID.randomUUID().toString();

    when(repository.existsByOriginalFileName(originalFileName))
            .thenReturn(true);
    doThrow(RuntimeException.class).when(storageService).copyIsToFileAndSetExpiresTag(any(), any());

    verify(repository, never()).save(any());

    assertThrows(RuntimeException.class, () -> instance.uploadFile(is, originalFileName));
  }

  @Test
  public void testDownloadFileSuccessful() {

    final InputStream is = createRandomContentIs();

    final String originalFileName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setState(FileState.READY);
    testFileDescription.setFileName(fileName);
    when(repository.findFirstByOriginalFileName(originalFileName)).thenReturn(Optional.of(testFileDescription));

    when(storageService.openIsToFile(fileName)).thenReturn(is);

    final InputStream result = instance.downloadFile(originalFileName);

    assertSame(is, result);
  }

  @Test
  public void testDownloadShouldFailBecauseNotFound() {

    final String originalFileName = UUID.randomUUID().toString();

    assertThrows(FileNotFoundException.class, () -> instance.downloadFile(originalFileName));
  }

  @Test
  public void testDownloadShouldFailBecauseIndexingState() {

    testDownloadShouldFailByStateInternal(FileState.INDEXING);
  }

  @Test
  public void testDownloadShouldFailBecauseDeletingState() {

    testDownloadShouldFailByStateInternal(FileState.DELETING);
  }

  @Test
  public void testDownloadShouldFailBecauseErorState() {

    testDownloadShouldFailByStateInternal(FileState.ERROR);
  }

  @Test
  public void testGetFilesByWordShouldDoLowerCase() {

    final String word = UUID.randomUUID().toString();

    final PageRequest pageRequest = PageRequest.of(0, 10);

    when(repository.findAllByEntriesWord(word.toLowerCase(), pageRequest))
            .thenReturn(Page.empty());

    instance.getFilesByWord(word, pageRequest);

    verify(repository).findAllByEntriesWord(word.toLowerCase(), pageRequest);
  }

  @Test
  public void testDeleteFileSuccessful() {

    final String originalFileName = UUID.randomUUID().toString();
    final String fileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setId(RandomUtils.nextLong());
    testFileDescription.setState(FileState.READY);
    testFileDescription.setFileName(fileName);
    testFileDescription.setOriginalFileName(originalFileName);

    when(repository.findFirstByOriginalFileName(originalFileName))
            .thenReturn(Optional.of(testFileDescription));

    final FileDescriptionOutDto result = instance.deleteFile(originalFileName);

    final ArgumentCaptor<FileDescription> fileDescriptionCaptor =
            ArgumentCaptor.forClass(FileDescription.class);

    verify(storageService).deleteFile(fileName);
    verify(repository).save(fileDescriptionCaptor.capture());
    final FileDescription captured = fileDescriptionCaptor.getValue();

    assertEquals(originalFileName, result.getOriginalFileName());
    assertEquals(FileState.DELETING.name(), result.getState());

    assertEquals(testFileDescription.getId(), captured.getId());
    assertEquals(testFileDescription.getOriginalFileName(), captured.getOriginalFileName());
    assertEquals(testFileDescription.getFileName(), captured.getFileName());
    assertEquals(FileState.DELETING, captured.getState());
  }

  @Test
  public void testDeleteShouldFailBecauseNotFound() {

    final String originalFileName = UUID.randomUUID().toString();

    assertThrows(FileNotFoundException.class, () -> instance.deleteFile(originalFileName));
  }

  @Test
  public void testDeleteShouldFailBecauseIndexingState() {

    testDeleteShouldFailByStateInternal(FileState.INDEXING);
  }

  @Test
  public void testDeleteShouldFailBecauseDeletingState() {

    testDeleteShouldFailByStateInternal(FileState.DELETING);
  }

  private void testDeleteShouldFailByStateInternal(final FileState state) {
    final String originalFileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setState(state);

    when(repository.findFirstByOriginalFileName(originalFileName))
            .thenReturn(Optional.of(testFileDescription));

    assertThrows(IllegalFileStateException.class, () -> instance.deleteFile(originalFileName));
  }

  private void testDownloadShouldFailByStateInternal(final FileState state) {
    final String originalFileName = UUID.randomUUID().toString();

    final FileDescription testFileDescription = new FileDescription();
    testFileDescription.setState(state);

    when(repository.findFirstByOriginalFileName(originalFileName))
            .thenReturn(Optional.of(testFileDescription));

    assertThrows(IllegalFileStateException.class, () -> instance.downloadFile(originalFileName));
  }

  private InputStream createRandomContentIs() {
    final String content = UUID.randomUUID().toString();
    return IOUtils.toInputStream(content, Charset.defaultCharset());
  }
}