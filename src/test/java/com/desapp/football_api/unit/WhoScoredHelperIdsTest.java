package com.desapp.football_api.unit;

import com.desapp.football_api.utils.WhoScoredHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class WhoScoredHelperIdsTest {

    @Test
    void getIdsFromResponse_validJson_extractsUniqueIds() throws JsonProcessingException {
        String json = "{" +
                "\"playerTableStats\":[{" +
                "\"playerId\":1},{\"playerId\":2},{\"playerId\":1}]}";
        List<Long> ids = WhoScoredHelper.getIdsFromResponse(json);
        assertEquals(2, ids.size());
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
    }

    @Test
    void getIdsFromResponse_missingArray_returnsEmpty() throws JsonProcessingException {
        String json = "{\"nope\":[]}";
        List<Long> ids = WhoScoredHelper.getIdsFromResponse(json);
        assertTrue(ids.isEmpty());
    }

    @Test
    void getIdsFromResponse_invalidJson_throws() {
        assertThrows(JsonProcessingException.class, () -> WhoScoredHelper.getIdsFromResponse("not-json"));
    }
}
