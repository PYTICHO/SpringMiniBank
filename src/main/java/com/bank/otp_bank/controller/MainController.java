package com.bank.otp_bank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MainController {
    
    @GetMapping("/checkAuth")
    public String checkAuth() {
        return "You authenticated!";
    }
    
}
