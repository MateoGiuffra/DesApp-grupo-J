package com.desapp.football_api.service;

import com.desapp.football_api.model.HelloEntity;
import com.desapp.football_api.repository.HelloRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class Hello {
    private final HelloRepository helloRepository;

    public HelloEntity setNewHello(HelloEntity hello){
        return helloRepository.save(hello);
    }
}
