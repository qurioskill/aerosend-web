package io.aerosend.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aerosend.web.client.CoreApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

@Controller
public class DomainsController {

    private final CoreApiClient core;
    private final ObjectMapper json;

    public DomainsController(CoreApiClient core, ObjectMapper json) {
        this.core = core;
        this.json = json;
    }

    @GetMapping("/domains")
    public String list(@RequestAttribute("customerId") long customerId, Model model) {
        model.addAttribute("domains", core.domains(customerId));
        return "domains/list";
    }

    @GetMapping("/domains/{id}")
    public String detail(@PathVariable long id, Model model) throws JsonProcessingException {
        model.addAttribute("domainId", id);
        model.addAttribute("statsJson", json.writeValueAsString(core.stats(id, 14)));
        return "domains/detail";
    }
}
