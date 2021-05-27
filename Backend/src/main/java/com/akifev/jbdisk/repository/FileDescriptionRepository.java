package com.akifev.jbdisk.repository;

import com.akifev.jbdisk.model.FileDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FileDescriptionRepository extends CrudRepository<FileDescription, String> {

  Page<FileDescription> findAll(Pageable pageable);

  Optional<FileDescription> findFirstByOriginalFileName(String fileName);

  Page<FileDescription> findAllByEntriesWord(String word, Pageable pageable);

  boolean existsByOriginalFileName(String originalFileName);

}
