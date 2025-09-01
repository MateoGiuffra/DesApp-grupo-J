package com.desapp.football_api.repository;

import com.desapp.football_api.model.HelloEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelloRepository extends JpaRepository<HelloEntity, Long> {
}
