package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.generic.NotFoundException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class WhoScoredService {

    public String fetchJSONString(String url) throws java.io.IOException, InterruptedException {
        HttpClient client = java.net.http.HttpClient.newHttpClient();
        HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .header("Referer", "https://es.whoscored.com/")
                .header("Cookie", "_fbp=fb.1.1758064465942.476798853467207590; _xpid=6325398004; _xpkey=kCSpXKfdThqha20bNjE_uvq4T__NKd9J; _adm-gpp=DBAA; _gid=GA1.2.1444691027.1758064509; ...") // recorta la cookie si es necesario
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public Document fetchPage(String url) throws IOException {
        return org.jsoup.Jsoup.connect(url)
                .header("Accept", "application/json")
                .header("Content-Type", "html/text")
                .header("Connection", "keep-alive")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .header("Referer", "https://es.whoscored.com/")
                .header("Cookie", "_fbp=fb.1.1758064465942.476798853467207590; _xpid=6325398004; _xpkey=kCSpXKfdThqha20bNjE_uvq4T__NKd9J; _adm-gpp=DBAA; _gid=GA1.2.1444691027.1758064509; ...") // recorta la cookie si es necesario
                .get();
    }

    public String getIdFromFirstResult(String name, Runnable exception) throws IOException {
        String normalizedName = normalizeName(name);
        String url = "https://whoscored.com/search/?t=" + normalizedName;
        Document doc = fetchPage(url);

        Element firstTable = doc.selectFirst("table");
        validateSearchElement(firstTable, exception);
        Element anchor = firstTable.selectFirst("a");
        validateSearchElement(anchor, exception);

        String href = anchor.attr("href");
        return href.replaceAll(".*/(players|teams)/(\\d+)/.*", "$2");
    }

    private String normalizeName(String name) {
        return name.trim()
                .toLowerCase()
                .replace(" ", "%20");
    }

    private void validateSearchElement(Element element, Runnable exception) {
        if (element == null) {
            exception.run();
            throw new NotFoundException("Resource not found");
        }
    }

}