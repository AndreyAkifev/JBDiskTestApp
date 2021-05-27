package com.akifev.jbdisk.rest.converter;

import com.akifev.jbdisk.model.FileDescription;
import com.akifev.jbdisk.rest.dto.FileDescriptionOutDto;
import org.springframework.stereotype.Component;

@Component
public class FileConverter {

  public FileDescriptionOutDto convert(final FileDescription entity) {

    final FileDescriptionOutDto dto = new FileDescriptionOutDto();

    dto.setOriginalFileName(entity.getOriginalFileName());
    dto.setState(entity.getState().toString());
    dto.setErrorCause(entity.getErrorCause());

    return dto;
  }

}
