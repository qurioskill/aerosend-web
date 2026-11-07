package io.aerosend.web.controller;

import io.aerosend.web.client.CoreApiClient;
import io.aerosend.web.client.CoreApiClient.DomainSummary;
import io.aerosend.web.client.CoreApiClient.DomainView;
import io.aerosend.web.client.CoreApiClient.WindowCounts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    private static final String UNAVAILABLE = "Figures are unavailable right now";
    private static final String SORT_LABEL = "Sorted by bounce rate, last 3 complete days";
    private static final LocalDate DAY = LocalDate.of(2026, 11, 4);
    private static final DomainView BUSY = new DomainView(1L, "busy.example", "HEALTHY", true);
    private static final DomainView QUIET = new DomainView(2L, "quiet.example", "HEALTHY", true);

    @Autowired MockMvc mvc;
    @MockBean CoreApiClient core;

    private String page() throws Exception {
        return mvc.perform(get("/").requestAttr("customerId", 1L).locale(Locale.CANADA))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private static DomainSummary busySummary(Double placement, LocalDate placementDay) {
        return new DomainSummary(1L, new WindowCounts(2100L, 54L, 27L), new WindowCounts(450L, 16L, 3L),
            placement, placementDay, DAY);
    }

    @Test
    void rendersTheTableWithItsSortLabelAndDate() throws Exception {
        when(core.domains(1L)).thenReturn(List.of(BUSY));
        when(core.domainSummaries(1L)).thenReturn(List.of(busySummary(0.72, DAY)));

        assertThat(page()).contains(SORT_LABEL, "Data through", "2026-11-04", "busy.example",
                "href=\"/domains/1\"", "3.6%", "2.6%", "1.3%", "2100", "72.0%")
            .doesNotContain(UNAVAILABLE, "Not enough data", "Not tested");
    }

    @Test
    void aDomainWithoutPlacementShowsNotTested() throws Exception {
        when(core.domains(1L)).thenReturn(List.of(BUSY));
        when(core.domainSummaries(1L)).thenReturn(List.of(busySummary(null, null)));

        assertThat(page()).contains("Not tested");
    }

    @Test
    void domainsWithoutEnoughDataShowAReasonAndNoRate() throws Exception {
        var quiet = new DomainSummary(2L, new WindowCounts(0L, 0L, 0L), new WindowCounts(0L, 0L, 0L), null, null, null);
        when(core.domains(1L)).thenReturn(List.of(QUIET));
        when(core.domainSummaries(1L)).thenReturn(List.of(quiet));

        assertThat(page()).contains("Not enough data", "quiet.example", "No sends in 14 days", "href=\"/domains/2\"")
            .doesNotContain("%", "Data through", SORT_LABEL, UNAVAILABLE);
    }

    @Test
    void thePageGivesNoVerdict() throws Exception {
        when(core.domains(1L)).thenReturn(List.of(BUSY));
        when(core.domainSummaries(1L)).thenReturn(List.of(busySummary(0.72, DAY)));

        assertThat(page().toLowerCase(Locale.ROOT)).doesNotContain("worst", "needs attention", "at risk", "badge");
    }

    @Test
    void aFailedSummaryCallFallsBackToThePlainList() throws Exception {
        when(core.domains(1L)).thenReturn(List.of(BUSY, QUIET));
        when(core.domainSummaries(1L)).thenThrow(new ResourceAccessException("timed out"));

        assertThat(page()).contains(UNAVAILABLE, "busy.example", "quiet.example", "href=\"/domains/1\"")
            .doesNotContain(SORT_LABEL, "Not enough data");
    }

    @Test
    void aCoreWithoutTheRouteFallsBackToThePlainList() throws Exception {
        when(core.domains(1L)).thenReturn(List.of(BUSY));
        when(core.domainSummaries(1L)).thenThrow(HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThat(page()).contains(UNAVAILABLE, "busy.example");
    }

    @Test
    void nullCountsFallBackToThePlainList() throws Exception {
        var drifted = new DomainSummary(1L, new WindowCounts(null, null, null), new WindowCounts(450L, 16L, 3L), null, null, DAY);
        when(core.domains(1L)).thenReturn(List.of(BUSY));
        when(core.domainSummaries(1L)).thenReturn(List.of(drifted));

        assertThat(page()).contains(UNAVAILABLE, "busy.example").doesNotContain(SORT_LABEL);
    }

    @Test
    void noDomainsShowsTheEmptyState() throws Exception {
        when(core.domains(1L)).thenReturn(List.of());
        when(core.domainSummaries(1L)).thenReturn(List.of());

        assertThat(page()).contains("No domains yet.").doesNotContain(SORT_LABEL, "Not enough data", UNAVAILABLE);
    }

    @Test
    void aFailedDomainListFailsThePage() {
        when(core.domains(1L)).thenThrow(new RestClientException("core is down"));

        assertThatThrownBy(() -> mvc.perform(get("/").requestAttr("customerId", 1L)))
            .hasRootCauseInstanceOf(RestClientException.class);
        verify(core, never()).domainSummaries(anyLong());
    }
}
