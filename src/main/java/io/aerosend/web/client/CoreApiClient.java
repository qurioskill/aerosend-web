package io.aerosend.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

/** Thin client over aerosend-core's /api/v1. Mirrors core's DTOs; keep in sync when core changes. */
@Component
public class CoreApiClient {

    public record DomainView(Long id, String name, String burnStatus, boolean dnsVerified) {}
    public record DailyStats(LocalDate day, long sent, long bounces, long replies,
                             Double warmupScore, Double inboxPlacement) {}

    private final RestClient http;

    public CoreApiClient(@Value("${aerosend.core-api.base-url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<DomainView> domains(long customerId) {
        return http.get().uri("/api/v1/domains?customerId={id}", customerId)
            .retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public List<DailyStats> stats(long domainId, int days) {
        return http.get().uri("/api/v1/domains/{id}/stats?days={d}", domainId, days)
            .retrieve().body(new ParameterizedTypeReference<>() {});
    }
}
