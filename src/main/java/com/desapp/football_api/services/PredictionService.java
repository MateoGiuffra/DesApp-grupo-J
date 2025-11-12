package com.desapp.football_api.services;

import com.desapp.football_api.model.prediction.PredictionResult;

public interface PredictionService {

    PredictionResult prediccionPoisson(Long homeTeamId, Long awayTeamId);

    PredictionResult prediccionPoisson(String homeTeamName, String awayTeamName);
}
