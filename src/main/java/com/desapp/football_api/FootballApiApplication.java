package com.desapp.football_api;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.SQLException;

@EnableScheduling
@SpringBootApplication
public class FootballApiApplication {
    public static void main(String[] args) {
        try {
            Server.createTcpServer("-tcp", "-tcpAllowOthers", "-ifNotExists").start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SpringApplication.run(FootballApiApplication.class, args);
    }
}
