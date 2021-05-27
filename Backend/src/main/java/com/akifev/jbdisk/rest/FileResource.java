package com.akifev.jbdisk.rest;

import com.akifev.jbdisk.rest.dto.FileDescriptionOutDto;
import com.akifev.jbdisk.service.FileDescriptionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/files")
public class FileResource {

  private final FileDescriptionService service;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FileDescriptionOutDto create(@RequestParam("file") MultipartFile multipartFile) throws IOException {

    return service.uploadFile(multipartFile.getInputStream(), multipartFile.getOriginalFilename());
  }

  @DeleteMapping("/{name}")
  public FileDescriptionOutDto delete(@PathVariable final String name) {

    return service.deleteFile(name);
  }

  @GetMapping
  public Page<FileDescriptionOutDto> queryFiles(@RequestParam(required = false) final String word, final Pageable pageable) {

    if (StringUtils.isBlank(word)) {
      return service.getFiles(pageable);
    } else {
      return service.getFilesByWord(word, pageable);
    }
  }

  @GetMapping("/{name}")
  public ResponseEntity<Resource> downloadFile(@PathVariable final String name) {

    final InputStream inputStream = service.downloadFile(name);

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(inputStream));
  }

}
