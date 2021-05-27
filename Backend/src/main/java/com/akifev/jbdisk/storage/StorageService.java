package com.akifev.jbdisk.storage;

import java.io.InputStream;

public interface StorageService {

  void copyIsToFileAndSetExpiresTag(InputStream is, String fileName);

  InputStream openIsToFile(String fileName);

  void deleteFile(String fileName);

}
