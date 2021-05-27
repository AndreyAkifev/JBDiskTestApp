package com.akifev.jbdisk.rest.dto;

import lombok.Data;

@Data
public class FileDescriptionOutDto {

  private String originalFileName;

  private String state;

  private String errorCause;

}
