package io.aerosend.web.controller;

import io.aerosend.web.client.CoreApiClient.DomainSummary;
import io.aerosend.web.client.CoreApiClient.DomainView;
import io.aerosend.web.client.CoreApiClient.WindowCounts;
import io.aerosend.web.controller.DomainTable.NoDataRow;
import io.aerosend.web.controller.DomainTable.SortedRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class DomainTableTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 4);

    private static DomainView domain(long id, String name) {
        return new DomainView(id, name, "HEALTHY", true);
    }

    private static WindowCounts counts(long sent, long bounces, long replies) {
        return new WindowCounts(sent, bounces, replies);
    }

    private static DomainSummary summary(long id, WindowCounts last14Days, WindowCounts last3Days) {
        return new DomainSummary(id, last14Days, last3Days, null, null, DAY);
    }

    /** A summary that reaches the sorted group, with the given 3-day bounces out of 100 sends. */
    private static DomainSummary sortable(long id, long bounces3Days) {
        return summary(id, counts(1000, 20, 10), counts(100, bounces3Days, 1));
    }

    private static List<String> sortedNames(DomainTable table) {
        return table.sorted().stream().map(SortedRow::name).toList();
    }

    @Test
    void bouncesWithNoSendsAreADataProblem() {
        var table = DomainTable.from(List.of(domain(1, "a.example")),
            List.of(summary(1, counts(0, 3, 0), counts(0, 1, 0))));

        assertThat(table.sorted()).isEmpty();
        assertThat(table.notEnoughData()).containsExactly(new NoDataRow(1, "a.example", "Bounces recorded with no sends"));
    }

    @Test
    void noSendsIn14DaysIsNotEnoughData() {
        var table = DomainTable.from(List.of(domain(1, "a.example")),
            List.of(summary(1, counts(0, 0, 0), counts(0, 0, 0))));

        assertThat(table.sorted()).isEmpty();
        assertThat(table.notEnoughData()).containsExactly(new NoDataRow(1, "a.example", "No sends in 14 days"));
    }

    @Test
    void aDomainWithNoSummaryHasNoData() {
        var table = DomainTable.from(List.of(domain(1, "a.example")), List.of());

        assertThat(table.sorted()).isEmpty();
        assertThat(table.notEnoughData()).containsExactly(new NoDataRow(1, "a.example", "No sends in 14 days"));
    }

    @Test
    void twentyNineSendsIsBelowTheMinimum() {
        var table = DomainTable.from(List.of(domain(1, "a.example")),
            List.of(summary(1, counts(500, 10, 5), counts(29, 5, 1))));

        assertThat(table.sorted()).isEmpty();
        assertThat(table.notEnoughData()).containsExactly(new NoDataRow(1, "a.example", "Only 29 sends in the last 3 days"));
    }

    @Test
    void reasonReadsNaturallyForZeroAndOneSends() {
        var table = DomainTable.from(List.of(domain(1, "a.example"), domain(2, "b.example")),
            List.of(summary(1, counts(500, 10, 5), counts(0, 0, 0)), summary(2, counts(500, 10, 5), counts(1, 0, 0))));

        assertThat(table.notEnoughData()).containsExactly(
            new NoDataRow(1, "a.example", "No sends in the last 3 days"),
            new NoDataRow(2, "b.example", "Only 1 send in the last 3 days"));
    }

    @Test
    void thirtySendsReachesTheSortedGroup() {
        var table = DomainTable.from(List.of(domain(1, "a.example")),
            List.of(summary(1, counts(500, 10, 5), counts(30, 3, 1))));

        assertThat(table.notEnoughData()).isEmpty();
        assertThat(sortedNames(table)).containsExactly("a.example");
    }

    @Test
    void ratesComeFromTheSummedCounts() {
        var summary = new DomainSummary(1L, counts(2100, 54, 27), counts(450, 16, 3), 0.72, DAY, DAY);

        var row = DomainTable.from(List.of(domain(1, "a.example")), List.of(summary)).sorted().get(0);

        assertThat(row.bounceRate3Days()).isCloseTo(16.0 / 450, within(1e-9));
        assertThat(row.bounceRate14Days()).isCloseTo(54.0 / 2100, within(1e-9));
        assertThat(row.replyRate14Days()).isCloseTo(27.0 / 2100, within(1e-9));
        assertThat(row.sent14Days()).isEqualTo(2100);
        assertThat(row.latestPlacement()).isEqualTo(0.72);
        assertThat(row.latestPlacementDay()).isEqualTo(DAY);
    }

    @Test
    void sortsByThreeDayBounceRateHighestFirst() {
        var table = DomainTable.from(
            List.of(domain(1, "low.example"), domain(2, "high.example"), domain(3, "mid.example")),
            List.of(sortable(1, 1), sortable(2, 9), sortable(3, 4)));

        assertThat(sortedNames(table)).containsExactly("high.example", "mid.example", "low.example");
    }

    @Test
    void equalRatesFallBackToTheDomainName() {
        var table = DomainTable.from(
            List.of(domain(1, "b.example"), domain(2, "a.example")),
            List.of(sortable(1, 5), sortable(2, 5)));

        assertThat(sortedNames(table)).containsExactly("a.example", "b.example");
    }

    @Test
    void notEnoughDataIsOrderedByName() {
        var table = DomainTable.from(List.of(domain(1, "b.example"), domain(2, "a.example")), List.of());

        assertThat(table.notEnoughData()).extracting(NoDataRow::name).containsExactly("a.example", "b.example");
    }

    @Test
    void dataThroughIsTheLatestDayAcrossBothGroups() {
        var quiet = new DomainSummary(1L, counts(50, 0, 0), counts(5, 0, 0), null, null, DAY);
        var older = new DomainSummary(2L, counts(1000, 20, 10), counts(100, 2, 1), null, null, DAY.minusDays(2));

        var table = DomainTable.from(List.of(domain(1, "a.example"), domain(2, "b.example")), List.of(quiet, older));

        assertThat(table.dataThrough()).isEqualTo(DAY);
    }

    @Test
    void dataThroughIsNullWhenNoDomainHasData() {
        var silent = new DomainSummary(1L, counts(0, 0, 0), counts(0, 0, 0), null, null, null);

        var table = DomainTable.from(List.of(domain(1, "a.example")), List.of(silent));

        assertThat(table.dataThrough()).isNull();
    }

    @Test
    void summariesForUnknownDomainsAreIgnored() {
        var stranger = new DomainSummary(99L, counts(1000, 20, 10), counts(100, 9, 1), null, null, DAY);

        var table = DomainTable.from(List.of(domain(1, "a.example")), List.of(stranger));

        assertThat(table.sorted()).isEmpty();
        assertThat(table.notEnoughData()).extracting(NoDataRow::id).containsExactly(1L);
        assertThat(table.dataThrough()).isNull();
    }

    @Test
    void aNullCountIsAContractError() {
        var drifted = summary(1, new WindowCounts(100L, null, 1L), counts(50, 1, 0));

        assertThatThrownBy(() -> DomainTable.from(List.of(domain(1, "a.example")), List.of(drifted)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aNullWindowIsAContractError() {
        var drifted = summary(1, counts(100, 1, 1), null);

        assertThatThrownBy(() -> DomainTable.from(List.of(domain(1, "a.example")), List.of(drifted)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aNullDomainIdIsAContractError() {
        var drifted = new DomainSummary(null, counts(100, 1, 1), counts(50, 1, 0), null, null, DAY);

        assertThatThrownBy(() -> DomainTable.from(List.of(domain(1, "a.example")), List.of(drifted)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aMissingBodyIsAContractError() {
        assertThatThrownBy(() -> DomainTable.from(List.of(domain(1, "a.example")), null))
            .isInstanceOf(IllegalStateException.class);
    }
}
