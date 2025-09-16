package com.desapp.football_api.service;

import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Service
public class WhoScoredService {

    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .build();
public String fetchPlayerPageHtml(String url) throws java.io.IOException, InterruptedException {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9")
                    .timeout(30_000)
                    .followRedirects(true)
                    .get();

            Element div = doc.selectFirst("div.col12-lg-12.col12-m-12.col12-s-12.col12-xs-12");

            if (div == null) {
                div = doc.selectFirst("div[class=\"col12-lg-12 col12-m-12 col12-s-12 col12-xs-12\"]");
            }

            if (div != null) {
                String innerHtml = div.html();
                System.out.println("Inner HTML: " + innerHtml);
                System.out.println("Full HTML: " + div.outerHtml());
                System.out.println("Text Content: " + div.text());
                return innerHtml;
            }
            return "Div not found";
        }
    public String fetchPlayerPageHtml() throws java.io.IOException, InterruptedException {
        return fetchPlayerPageHtml("https://es.whoscored.com/players/315227/show/erling-haaland");
    }
}