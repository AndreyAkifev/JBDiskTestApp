package com.akifev.jbdisk.exception;

import org.springframework.http.HttpStatus;

public class FileAlreadyExistsException extends ApplicationException {

  public FileAlreadyExistsException(final String fileName) {

    super(HttpStatus.CONFLICT, String.format("File is already exists: %s", fileName));
  }

}
