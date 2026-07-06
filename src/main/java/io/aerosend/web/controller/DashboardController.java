package io.aerosend.web.controller;

import io.aerosend.web.client.CoreApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

@Controller
public class DashboardController {

    private final CoreApiClient core;

    public DashboardController(CoreApiClient core) { this.core = core; }

    @GetMapping("/")
    public String dashboard(@RequestAttribute("customerId") long customerId, Model model) {
        var domains = core.domains(customerId);
        model.addAttribute("domains", domains);
        model.addAttribute("atRisk", domains.stream().filter(d -> !"HEALTHY".equals(d.burnStatus())).count());
        return "dashboard";
    }
}
