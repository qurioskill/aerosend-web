package io.aerosend.web.controller;

import io.aerosend.web.client.CoreApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.client.RestClientException;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final CoreApiClient core;

    public DashboardController(CoreApiClient core) { this.core = core; }

    @GetMapping("/")
    public String dashboard(@RequestAttribute("customerId") long customerId, Model model) {
        var domains = core.domains(customerId);
        DomainTable table = null;
        try {
            table = DomainTable.from(domains, core.domainSummaries(customerId));
        } catch (RestClientException | IllegalStateException e) {
            log.warn("Domain figures unavailable for customer {}", customerId, e);
        }
        model.addAttribute("domains", domains);
        model.addAttribute("table", table);
        model.addAttribute("figuresUnavailable", table == null);
        return "dashboard";
    }
}
