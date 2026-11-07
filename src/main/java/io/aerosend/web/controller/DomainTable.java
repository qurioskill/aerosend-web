package io.aerosend.web.controller;

import io.aerosend.web.client.CoreApiClient.DomainSummary;
import io.aerosend.web.client.CoreApiClient.DomainView;
import io.aerosend.web.client.CoreApiClient.WindowCounts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The dashboard's two groups of domains. {@code domains} is the authoritative list, so a domain with no
 * summary counts as having no data. A summary that breaks the contract raises IllegalStateException.
 */
public record DomainTable(List<SortedRow> sorted, List<NoDataRow> notEnoughData, LocalDate dataThrough) {

    /** The owner's choice. At this volume one bounce moves the rate by 3.3 points. */
    public static final int MIN_SENDS_LAST_3_DAYS = 30;

    public record SortedRow(long id, String name, double bounceRate3Days, double bounceRate14Days,
                            double replyRate14Days, long sent14Days,
                            Double latestPlacement, LocalDate latestPlacementDay) {}

    public record NoDataRow(long id, String name, String reason) {}

    public static DomainTable from(List<DomainView> domains, List<DomainSummary> summaries) {
        Map<Long, DomainSummary> summaryByDomain = indexValidated(summaries);
        List<SortedRow> sorted = new ArrayList<>();
        List<NoDataRow> notEnoughData = new ArrayList<>();
        LocalDate dataThrough = null;

        for (DomainView domain : domains) {
            DomainSummary summary = summaryByDomain.get(domain.id());
            String reason = noDataReason(summary);
            if (reason != null) {
                notEnoughData.add(new NoDataRow(domain.id(), domain.name(), reason));
            } else {
                sorted.add(sortedRow(domain, summary));
            }
            if (summary != null && summary.lastDataDay() != null
                    && (dataThrough == null || summary.lastDataDay().isAfter(dataThrough))) {
                dataThrough = summary.lastDataDay();
            }
        }

        sorted.sort(Comparator.comparingDouble(SortedRow::bounceRate3Days).reversed().thenComparing(SortedRow::name));
        notEnoughData.sort(Comparator.comparing(NoDataRow::name));
        return new DomainTable(List.copyOf(sorted), List.copyOf(notEnoughData), dataThrough);
    }

    private static Map<Long, DomainSummary> indexValidated(List<DomainSummary> summaries) {
        if (summaries == null) {
            throw new IllegalStateException("Core returned no summary body");
        }
        Map<Long, DomainSummary> byDomain = new HashMap<>();
        for (DomainSummary summary : summaries) {
            if (summary == null || summary.domainId() == null
                    || hasNullCount(summary.last14Days()) || hasNullCount(summary.last3Days())) {
                throw new IllegalStateException("Summary from core breaks the contract: " + summary);
            }
            byDomain.put(summary.domainId(), summary);
        }
        return byDomain;
    }

    private static boolean hasNullCount(WindowCounts counts) {
        return counts == null || counts.sent() == null || counts.bounces() == null || counts.replies() == null;
    }

    /** The reason this domain cannot be sorted, or null if it can. */
    private static String noDataReason(DomainSummary summary) {
        if (summary == null) {
            return "No sends in 14 days";
        }
        if (summary.last14Days().sent() == 0) {
            return summary.last14Days().bounces() > 0 ? "Bounces recorded with no sends" : "No sends in 14 days";
        }
        long sent3Days = summary.last3Days().sent();
        if (sent3Days >= MIN_SENDS_LAST_3_DAYS) {
            return null;
        }
        if (sent3Days == 0) {
            return "No sends in the last 3 days";
        }
        return "Only " + sent3Days + (sent3Days == 1 ? " send" : " sends") + " in the last 3 days";
    }

    private static SortedRow sortedRow(DomainView domain, DomainSummary summary) {
        WindowCounts last14Days = summary.last14Days();
        WindowCounts last3Days = summary.last3Days();
        return new SortedRow(domain.id(), domain.name(),
            rate(last3Days.bounces(), last3Days.sent()),
            rate(last14Days.bounces(), last14Days.sent()),
            rate(last14Days.replies(), last14Days.sent()),
            last14Days.sent(), summary.latestPlacement(), summary.latestPlacementDay());
    }

    private static double rate(long count, long sent) {
        return (double) count / sent;
    }
}
