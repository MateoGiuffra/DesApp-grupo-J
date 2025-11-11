package com.desapp.football_api.services;

import com.desapp.football_api.model.PredictionResult;

public interface PredictionService {

    PredictionResult prediccionPoisson(Long homeTeamId, Long awayTeamId);
}
