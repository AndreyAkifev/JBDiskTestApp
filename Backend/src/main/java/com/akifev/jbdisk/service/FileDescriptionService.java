package com.akifev.jbdisk.service;

import com.akifev.jbdisk.rest.dto.FileDescriptionOutDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;

public interface FileDescriptionService {

  FileDescriptionOutDto uploadFile(InputStream inputStream, String fileName);

  InputStream downloadFile(String fileName);

  Page<FileDescriptionOutDto> getFilesByWord(String word, Pageable pageable);

  Page<FileDescriptionOutDto> getFiles(Pageable pageRequest);

  FileDescriptionOutDto deleteFile(String fileName);

}
