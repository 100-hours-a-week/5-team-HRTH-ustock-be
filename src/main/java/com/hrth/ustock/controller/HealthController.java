package com.hrth.ustock.controller;

import io.sentry.spring.jakarta.EnableSentry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableSentry(dsn = "https://f4549cec259eb3cf4977fbe8960b9405@o4507837261021184.ingest.us.sentry.io/4507837264035840")
@RestController
@RequestMapping("/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Void> checkHealth() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
