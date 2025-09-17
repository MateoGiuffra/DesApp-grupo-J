package com.desapp.football_api.service;

import com.desapp.football_api.model.player.Player;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

@Service
public class WhoScoredService {

    public Document fetchPlayerPageHtml(String url) throws java.io.IOException, InterruptedException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.9")
                .header("Connection", "keep-alive")
                .header("Referer", "https://es.whoscored.com/")
                .header("Cookie", "puedes_copiar_una_cookie_real_de_tu_navegador")
                .timeout(30_000)
                .followRedirects(true)
                .get();
            return doc;

    }

public void setPlayerData(Player player, Document doc) {
    Element div = doc.selectFirst("col12-lg-10 col12-m-10 col12-s-9 col12-xs-8");

    if (div == null) {
        div = doc.selectFirst("div[class=\"col12-lg-10 col12-m-10 col12-s-9 col12-xs-8\"]");
    }

    if (div == null) {
        throw new IllegalStateException("HTML div principal no encontrado en el documento");
    }

    List<String> valores = new ArrayList<>();
    for (Element subDiv : div.select("div")) {
        String texto = subDiv.ownText().trim();
        if (!texto.isEmpty()) {
            if (texto.contains("años")) {
                Element hijo = subDiv.children().get(1);
                if (hijo != null) {
                    valores.add(hijo.text().trim());
                }
            } else {
                valores.add(texto);
            }
        }
    }
    System.out.println(valores);

    //System.out.println(div.selectFirst("div"));

    Element nameElement = div.selectFirst("div");
}

    public void setPlayerStats(Player player, Document doc){

    }


    public Player fetchPlayerPageHtml() throws java.io.IOException, InterruptedException {
        Document doc = fetchPlayerPageHtml("https://es.whoscored.com/players/315227/show/erling-haaland");
        Player player = new Player();
        setPlayerData(player, doc);
        setPlayerStats(player,doc);
        return player;
    }
}