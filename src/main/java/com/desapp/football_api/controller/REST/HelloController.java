package com.desapp.football_api.controller.REST;

import com.desapp.football_api.model.HelloEntity;
import com.desapp.football_api.service.Hello;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@CrossOrigin
@RequestMapping("/hello")
@AllArgsConstructor
public class HelloController {

    private final Hello service;

    @GetMapping()
    public String hello(){
        return "hello";
    }

    @PostMapping()
    public ResponseEntity<HelloEntity> createNewHello(@RequestBody String body){
        HelloEntity newHello = new HelloEntity(body);
        return ResponseEntity.ok(service.setNewHello(newHello));
    }
}
