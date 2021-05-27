package com.akifev.lambda.repository;

import com.akifev.lambda.model.FileDescription;
import com.akifev.lambda.model.FileState;
import com.amazon.rdsdata.client.RdsDataClient;

public class FileDescriptionRepository {

  private final RdsDataClient rds;

  public FileDescriptionRepository(RdsDataClient rds) {

    this.rds = rds;
  }

  public FileDescription findByFileName(final String fileName) {

    return rds.forSql("SELECT * FROM file_description WHERE file_name = :fileName")
            .withParameter("fileName", fileName)
            .execute()
            .mapToSingle(FileDescription.class);
  }

  public void updateState(final Long id, final FileState state) {

    rds.forSql("UPDATE file_description SET state = :state WHERE id = :id")
            .withParameter("id", id)
            .withParameter("state", state.name())
            .execute();
  }

  public void updateStateAndErrorCause(final Long id,
                                       final FileState state,
                                       final String errorCause) {

    rds.forSql("UPDATE file_description SET state = :state, error_cause = :errorCause WHERE id = :id")
            .withParameter("id", id)
            .withParameter("state", state.name())
            .withParameter("errorCause", errorCause)
            .execute();
  }

  public FileDescription findByFileNameAndState(final String fileName, final FileState state) {

    return rds.forSql("SELECT * FROM file_description WHERE file_name = :fileName AND state = :state")
            .withParameter("fileName", fileName)
            .withParameter("state", state.name())
            .execute()
            .mapToSingle(FileDescription.class);
  }

  public void deleteById(final Long id) {

    rds.forSql("DELETE FROM file_description WHERE id = :id")
            .withParameter("id", id)
            .execute();
  }

}
