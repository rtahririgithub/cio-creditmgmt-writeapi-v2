package com.telus.credit.health;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Readinessprobe{
    @GetMapping("/greeting")
    public String greeting() {
        return "hello";    
    } 
    @PostMapping("/greeting")
    public String greetingPost() {
        return "hello";    
    }     
}