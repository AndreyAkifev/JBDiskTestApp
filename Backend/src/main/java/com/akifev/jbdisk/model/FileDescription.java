package com.akifev.jbdisk.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "file_description")
public class FileDescription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "original_file_name", nullable = false, unique = true)
  private String originalFileName;

  @Column(name = "file_name", nullable = false, unique = true)
  private String fileName;

  @Column(name = "state", nullable = false)
  @Enumerated(EnumType.STRING)
  private FileState state;

  @Column(name = "error_cause")
  private String errorCause;

  @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
  private Set<FileEntry> entries;
}
