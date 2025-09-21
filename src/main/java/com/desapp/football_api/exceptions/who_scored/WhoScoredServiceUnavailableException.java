package com.desapp.football_api.exceptions.who_scored;

public class WhoScoredServiceUnavailableException extends RuntimeException {
    public WhoScoredServiceUnavailableException() {
        super(
                "WhoScored service is currently unavailable. Please try again later."
        );
    }
}
