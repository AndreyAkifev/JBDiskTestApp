package com.akifev.jbdisk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ApplicationException extends ResponseStatusException {

  public ApplicationException(final HttpStatus status, final String message) {

    super(status, message);
  }

}
