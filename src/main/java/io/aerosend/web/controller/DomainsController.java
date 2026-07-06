package io.aerosend.web.controller;

import io.aerosend.web.client.CoreApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

@Controller
public class DomainsController {

    private final CoreApiClient core;

    public DomainsController(CoreApiClient core) { this.core = core; }

    @GetMapping("/domains")
    public String list(@RequestAttribute("customerId") long customerId, Model model) {
        model.addAttribute("domains", core.domains(customerId));
        return "domains/list";
    }

    @GetMapping("/domains/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("domainId", id);
        model.addAttribute("stats", core.stats(id, 14));
        return "domains/detail";
    }
}
