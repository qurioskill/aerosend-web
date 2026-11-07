package io.aerosend.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/** Thin client over aerosend-core's /api/v1. Mirrors core's DTOs; keep in sync when core changes. */
@Component
public class CoreApiClient {

    public record DomainView(Long id, String name, String burnStatus, boolean dnsVerified) {}
    public record DailyStats(LocalDate day, long sent, long bounces, long replies,
                             Double warmupScore, Double inboxPlacement) {}

    /** Boxed on purpose: a field renamed in core arrives as null and can be detected, where long would read as 0. */
    public record WindowCounts(Long sent, Long bounces, Long replies) {}
    public record DomainSummary(Long domainId, WindowCounts last14Days, WindowCounts last3Days,
                                Double latestPlacement, LocalDate latestPlacementDay, LocalDate lastDataDay) {}

    private final RestClient http;

    public CoreApiClient(@Value("${aerosend.core-api.base-url}") String baseUrl,
                         @Value("${aerosend.core-api.connect-timeout}") Duration connectTimeout,
                         @Value("${aerosend.core-api.read-timeout}") Duration readTimeout) {
        var requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connectTimeout).build());
        requestFactory.setReadTimeout(readTimeout);
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public List<DomainView> domains(long customerId) {
        return http.get().uri("/api/v1/domains?customerId={id}", customerId)
            .retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public List<DailyStats> stats(long domainId, int days) {
        return http.get().uri("/api/v1/domains/{id}/stats?days={d}", domainId, days)
            .retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public List<DomainSummary> domainSummaries(long customerId) {
        return http.get().uri("/api/v1/domains/summary?customerId={id}", customerId)
            .retrieve().body(new ParameterizedTypeReference<>() {});
    }
}
