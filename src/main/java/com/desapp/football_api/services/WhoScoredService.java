package com.desapp.football_api.services;

import org.jsoup.nodes.Document;

public interface WhoScoredService {

    String fetchJSONString(String targetUrl);

    Document fetchPage(String targetUrl);

    String getIdFromFirstResult(String name, Runnable exception);

}
