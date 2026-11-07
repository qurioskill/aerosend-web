package io.aerosend.web.config;

import io.aerosend.web.client.CoreApiClient;
import io.aerosend.web.controller.DashboardController;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalCustomerConfigTest {

    @Nested
    @WebMvcTest(DashboardController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @ActiveProfiles("local")
    class WithLocalProfile {
        @Autowired MockMvc mvc;
        @MockBean CoreApiClient core;

        @Test
        void dashboardLoadsTheConfiguredCustomer() throws Exception {
            when(core.domains(1L)).thenReturn(List.of());
            mvc.perform(get("/")).andExpect(status().isOk());
            verify(core).domains(1L);
        }
    }

    @Nested
    @WebMvcTest(DashboardController.class)
    @AutoConfigureMockMvc(addFilters = false)
    class WithoutLocalProfile {
        @Autowired MockMvc mvc;
        @MockBean CoreApiClient core;

        @Test
        void dashboardFailsClosed() throws Exception {
            mvc.perform(get("/")).andExpect(status().isBadRequest());
            verifyNoInteractions(core);
        }
    }
}
