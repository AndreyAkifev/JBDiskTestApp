package com.akifev.lambda.exception;

public class TooLongWordException extends LambdaException {

  public TooLongWordException(final Integer maxLength) {

    super(String.format("Found the word which is longer than %s", maxLength));
  }

}
