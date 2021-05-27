package com.akifev.jbdisk.service.impl;

import com.akifev.jbdisk.exception.FileAlreadyExistsException;
import com.akifev.jbdisk.exception.FileNotFoundException;
import com.akifev.jbdisk.exception.IllegalFileStateException;
import com.akifev.jbdisk.model.FileDescription;
import com.akifev.jbdisk.model.FileState;
import com.akifev.jbdisk.repository.FileDescriptionRepository;
import com.akifev.jbdisk.rest.converter.FileConverter;
import com.akifev.jbdisk.rest.dto.FileDescriptionOutDto;
import com.akifev.jbdisk.service.FileDescriptionService;
import com.akifev.jbdisk.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileDescriptionServiceImpl implements FileDescriptionService {

  private final FileDescriptionRepository repository;

  private final StorageService storageService;

  private final FileConverter converter;

  @Override
  public FileDescriptionOutDto uploadFile(final InputStream inputStream, final String originalFileName) {

    if (repository.existsByOriginalFileName(originalFileName)) {
      throw new FileAlreadyExistsException(originalFileName);
    }

    final String fileName = String.format(".%s.%s", System.currentTimeMillis(), UUID.randomUUID());

    storageService.copyIsToFileAndSetExpiresTag(inputStream, fileName);

    final FileDescription fileDescription = new FileDescription();
    fileDescription.setState(FileState.INDEXING);
    fileDescription.setFileName(fileName);
    fileDescription.setOriginalFileName(originalFileName);

    return converter.convert(repository.save(fileDescription));
  }

  @Override
  public InputStream downloadFile(final String originalFileName) {

    final FileDescription found = repository.findFirstByOriginalFileName(originalFileName)
            .orElseThrow(() -> new FileNotFoundException(originalFileName));

    if (found.getState() != FileState.READY) {
      throw new IllegalFileStateException(originalFileName, found.getState());
    }

    return storageService.openIsToFile(found.getFileName());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<FileDescriptionOutDto> getFilesByWord(final String word, final Pageable pageable) {

    return repository.findAllByEntriesWord(word.toLowerCase(), pageable).map(converter::convert);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<FileDescriptionOutDto> getFiles(final Pageable pageRequest) {

    return repository.findAll(pageRequest).map(converter::convert);
  }

  @Override
  public FileDescriptionOutDto deleteFile(final String fileName) {

    final FileDescription found = repository.findFirstByOriginalFileName(fileName)
            .orElseThrow(() -> new FileNotFoundException(fileName));

    if (found.getState() != FileState.READY && found.getState() != FileState.ERROR) {
      throw new IllegalFileStateException(fileName, found.getState());
    }
    storageService.deleteFile(found.getFileName());
    found.setState(FileState.DELETING);
    repository.save(found);

    return converter.convert(found);
  }

}
