package com.desapp.football_api.services;

import com.desapp.football_api.exceptions.generic.NotFoundException;
import com.desapp.football_api.service.WhoScoredService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class WhoScoredServiceTest {

    @Test
    void getIdFromFirstResult_usesSecondTable_whenMultipleTables() {
        String html = "<html><body>" +
                "<table id='t1'><tr><td>Nope</td></tr></table>" +
                "<table id='t2'><tr><td><a href='/players/123/john-doe'>John</a></td></tr></table>" +
                "</body></html>";
        Document doc = Jsoup.parse(html);

        WhoScoredService spy = Mockito.spy(new WhoScoredService());
        doReturn(doc).when(spy).fetchPage(anyString());

        String id = spy.getIdFromFirstResult("John", () -> {
            throw new RuntimeException("should not be called");
        });
        assertEquals("123", id);
    }

    @Test
    void getIdFromFirstResult_whenNoTables_throwsNotFoundException() {
        String html = "<html><body><div>No table</div></body></html>";
        Document doc = Jsoup.parse(html);

        WhoScoredService spy = Mockito.spy(new WhoScoredService());
        doReturn(doc).when(spy).fetchPage(anyString());

        assertThrows(NotFoundException.class, () -> spy.getIdFromFirstResult("X", () -> { /* expected to run */ }));
    }
}
