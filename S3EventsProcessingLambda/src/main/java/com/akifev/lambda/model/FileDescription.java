package com.akifev.lambda.model;

/**
 * RDS SDK does not support camelCase or @JsonProperty annotation.
 * Proof: com.amazon.rdsdata.client.SetterWriter#buildSetterName(java.lang.String)
 *
 * That's why I'm using snake_case here.
 */
public class FileDescription {

  private Long id;

  private String original_file_name;

  private String file_name;

  private FileState state;

  private String error_cause;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getOriginal_file_name() {
    return original_file_name;
  }

  public void setOriginal_file_name(String original_file_name) {
    this.original_file_name = original_file_name;
  }

  public String getFile_name() {
    return file_name;
  }

  public void setFile_name(String file_name) {
    this.file_name = file_name;
  }

  public FileState getState() {
    return state;
  }

  public void setState(FileState state) {
    this.state = state;
  }

  public String getError_cause() {
    return error_cause;
  }

  public void setError_cause(String error_cause) {
    this.error_cause = error_cause;
  }
}
