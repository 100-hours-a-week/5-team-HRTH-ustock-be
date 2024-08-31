package com.hrth.ustock;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "ustock", version = "v1", description = "ustock API 명세서"))
public class UstockApplication {

    public static void main(String[] args) {
        SpringApplication.run(UstockApplication.class, args);
    }

}
