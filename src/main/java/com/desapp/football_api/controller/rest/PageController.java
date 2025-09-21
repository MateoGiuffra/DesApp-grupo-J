package com.desapp.football_api.controller.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String index() {
        System.out.println("entre!!");
        return "index";
    }
}
