package com.akifev.lambda.repository;

import com.amazon.rdsdata.client.RdsDataClient;

import java.util.Collection;
import java.util.Iterator;

public class FileEntryRepository {

  private final RdsDataClient rds;

  public FileEntryRepository(RdsDataClient rds) {

    this.rds = rds;
  }

  public void deleteAllByFileId(final Long id) {

    rds.forSql("DELETE FROM file_entry WHERE file_id = :id")
            .withParameter("id", id)
            .execute();
  }

  public void createAllIfConflictIgnore(final Long fileId, final Collection<String> words) {

    final StringBuilder sb = new StringBuilder();

    final Iterator<String> iterator = words.iterator();

    while (iterator.hasNext()) {
      sb.append(String.format("('%s', '%s')", iterator.next(), fileId));
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }

    rds.forSql(
            String.format(
                    "INSERT INTO file_entry (word, file_id) VALUES %s ON CONFLICT (word, file_id) DO NOTHING",
                    sb
            )
    ).execute();
  }
}
