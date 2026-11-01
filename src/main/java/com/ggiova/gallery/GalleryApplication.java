package com.ggiova.gallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GalleryApplication {
    public static void main(String[] args) {
        SpringApplication.run(GalleryApplication.class, args);
    }
}
