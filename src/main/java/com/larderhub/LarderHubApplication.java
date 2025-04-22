package com.larderhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LarderHubApplication {

  public static void main(String[] args) {
    SpringApplication.run(LarderHubApplication.class, args);
  }

}
