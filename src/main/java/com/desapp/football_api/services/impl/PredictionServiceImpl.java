package com.desapp.football_api.services.impl;

import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.prediction.Poisson;
import com.desapp.football_api.model.prediction.PredictionResult;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.services.PredictionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class PredictionServiceImpl implements PredictionService {
    private final TeamServiceImpl teamServiceImpl;


    @Override
    public PredictionResult prediccionPoisson(Long homeTeamId, Long awayTeamId) {
        CompletableFuture<Team> homeTeamFuture = CompletableFuture.supplyAsync(() ->
                teamServiceImpl.getOrScrapeTeamById(homeTeamId, StatsType.Current));
        CompletableFuture<Team> awayTeamFuture = CompletableFuture.supplyAsync(() ->
                teamServiceImpl.getOrScrapeTeamById(awayTeamId, StatsType.Current));

        try {
            CompletableFuture.allOf(homeTeamFuture, awayTeamFuture).join();
            Poisson poisson = new Poisson();
            return poisson.prediccionPoisson(homeTeamFuture.get(), awayTeamFuture.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException("Prediction interrupted", e);
        } catch (ExecutionException e) {
            // Unwrap the cause of the ExecutionException
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause; // Re-throw the original RuntimeException (e.g., TeamNotFoundException)
            } else {
                throw new RuntimeException("Error during team retrieval", cause);
            }
        }
    }

    @Override
    public PredictionResult prediccionPoisson(String homeTeamName, String awayTeamName) {
        CompletableFuture<Team> homeTeamFuture = CompletableFuture.supplyAsync(() ->
                teamServiceImpl.getOrScrapeTeamByName(homeTeamName, StatsType.Current));
        CompletableFuture<Team> awayTeamFuture = CompletableFuture.supplyAsync(() ->
                teamServiceImpl.getOrScrapeTeamByName(awayTeamName, StatsType.Current));

        try {
            CompletableFuture.allOf(homeTeamFuture, awayTeamFuture).join();
            Poisson poisson = new Poisson();
            return poisson.prediccionPoisson(homeTeamFuture.get(), awayTeamFuture.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException("Prediction interrupted", e);
        } catch (ExecutionException e) {
            // Unwrap the cause of the ExecutionException
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause; // Re-throw the original RuntimeException (e.g., TeamNotFoundException)
            } else {
                throw new RuntimeException("Error during team retrieval", cause);
            }
        }
    }


}
