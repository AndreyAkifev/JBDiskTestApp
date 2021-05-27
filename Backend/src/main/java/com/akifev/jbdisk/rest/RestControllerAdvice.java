package com.akifev.jbdisk.rest;

import com.akifev.jbdisk.exception.ApplicationException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@ControllerAdvice
public class RestControllerAdvice extends ResponseEntityExceptionHandler {

  private static final String EMPTY_MST = "";
  public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

  @ExceptionHandler({ApplicationException.class})
  protected ResponseEntity<Object> handleApplicationException(final ApplicationException ex,
                                                              final WebRequest request) throws Exception {

    return handleException(ex, request);
  }

  @ExceptionHandler({Exception.class})
  protected ResponseEntity<WebException> handleInternalException(final Exception e,
                                                                 final ServletWebRequest servletWebRequest) {

    log.error("", e);

    final WebException webException = new WebException();

    webException.setTimestamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
    webException.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    webException.setError(INTERNAL_SERVER_ERROR);
    webException.setMessage(EMPTY_MST);
    webException.setPath(servletWebRequest.getRequest().getRequestURI());
    return new ResponseEntity<>(webException, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }


  @Data
  private static class WebException {

    private String timestamp;

    private int status;

    private String error;

    private String message;

    private String path;
  }
}
