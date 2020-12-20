package com.adtech.books.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BasicController {

    @RequestMapping(value = "/welcome", method = RequestMethod.GET)
    public ResponseEntity welcome(){
        return ResponseEntity
                .ok()
                .body("Welcome to Book App !!!");
    }
}
