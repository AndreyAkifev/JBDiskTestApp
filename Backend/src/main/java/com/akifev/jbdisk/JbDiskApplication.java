package com.akifev.jbdisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.akifev.jbdisk.properties"})
public class JbDiskApplication {

  public static void main(String[] args) {

    SpringApplication.run(JbDiskApplication.class, args);
  }

}
