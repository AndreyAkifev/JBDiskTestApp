package com.akifev.jbdisk.exception;

import com.akifev.jbdisk.model.FileState;
import org.springframework.http.HttpStatus;

public class IllegalFileStateException extends ApplicationException {

  public IllegalFileStateException(final String fileName, final FileState currentState) {

    super(HttpStatus.CONFLICT, String.format("%s has an illegal state: %s", fileName, currentState));
  }

}
