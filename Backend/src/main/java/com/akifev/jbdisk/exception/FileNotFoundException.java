package com.akifev.jbdisk.exception;

import org.springframework.http.HttpStatus;

public class FileNotFoundException extends ApplicationException {

  public FileNotFoundException(final String fileName) {

    super(HttpStatus.NOT_FOUND, String.format("File %s not found", fileName));
  }
}
