package io.aerosend.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aerosend.web.client.CoreApiClient;
import io.aerosend.web.client.CoreApiClient.DailyStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DomainsController.class)
@AutoConfigureMockMvc(addFilters = false)
class DomainsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockBean CoreApiClient core;

    @Test
    void detailPassesStatsToTheChartAsJson() throws Exception {
        when(core.stats(7L, 14)).thenReturn(List.of(new DailyStats(LocalDate.of(2026, 11, 1), 100, 2, 3, 90.0, 0.9)));

        var model = mvc.perform(get("/domains/7")).andExpect(status().isOk()).andReturn().getModelAndView().getModel();

        var row = json.readTree((String) model.get("statsJson")).get(0);
        assertThat(row.get("day").asText()).isEqualTo("2026-11-01");
        assertThat(row.get("sent").asLong()).isEqualTo(100);
        assertThat(row.get("inboxPlacement").asDouble()).isEqualTo(0.9);
    }
}
