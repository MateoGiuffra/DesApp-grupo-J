package com.desapp.football_api;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.SQLException;
import java.time.Duration;

@EnableScheduling
@EnableCaching
@SpringBootApplication
public class FootballApiApplication {

    @Bean
    public CacheManager cacheManager() {
        // Caffeine cache manager with 1 minute TTL and a reasonable maximum size.
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("serviceCache");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(500));
        return cacheManager;
    }

    public static void main(String[] args) {
        try {
            Server.createTcpServer("-tcp", "-tcpAllowOthers", "-ifNotExists").start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SpringApplication.run(FootballApiApplication.class, args);
    }
}
