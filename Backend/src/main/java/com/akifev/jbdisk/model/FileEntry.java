package com.akifev.jbdisk.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "file_entry", uniqueConstraints = @UniqueConstraint(columnNames = {"word", "file_id"}))
public class FileEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "word", nullable = false)
  private String word;

  @ManyToOne
  @JoinColumn(name = "file_id")
  private FileDescription file;

}
