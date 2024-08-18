package com.hrth.ustock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/main")
public class MainController {
    @GetMapping("/news")
    public ResponseEntity<?> news() {
        return null;
    }

    @GetMapping("/market")
    public ResponseEntity<?> market() {
        return null;
    }
}
