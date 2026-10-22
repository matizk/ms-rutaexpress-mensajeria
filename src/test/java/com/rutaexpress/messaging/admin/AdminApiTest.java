package com.rutaexpress.messaging.admin;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rutaexpress.messaging.receipts.ReceiptController;
import com.rutaexpress.messaging.receipts.OperatorConfirmationController;
import com.rutaexpress.messaging.receipts.ReceiptRepository;
import com.rutaexpress.messaging.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({RabbitAdminController.class, ReceiptController.class, OperatorConfirmationController.class})
@Import({SecurityConfig.class, ApiErrorHandler.class})
class AdminApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean RabbitAdminService service;
    @MockitoBean ReceiptRepository receipts;
    @MockitoBean JwtDecoder decoder;
    @Test void rejectsAnonymousAdministration() throws Exception {
        mvc.perform(post("/api/rabbitmq/queues").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"rutaexpress.demo\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test void rejectsNonAdminToken() throws Exception {
        mvc.perform(get("/api/messaging/summary").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }
    @Test void rejectsEmptyNameAndInvalidTtl() throws Exception {
        for (String body : new String[]{"{\"name\":\"\"}", "{\"name\":\"rutaexpress.demo\",\"ttlMillis\":-1}"}) {
            mvc.perform(post("/api/rabbitmq/queues").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
    @Test void rejectsUnknownExchangeType() throws Exception {
        mvc.perform(post("/api/rabbitmq/exchanges").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"rutaexpress.demo\",\"type\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test void createsValidatedQueueForAdministrator() throws Exception {
        mvc.perform(post("/api/rabbitmq/queues").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"rutaexpress.demo\",\"ttlMillis\":1000}"))
                .andExpect(status().isCreated());
        verify(service).createQueue("rutaexpress.demo", 1000);
    }
    @Test void operatorCanQueryOnlyAnAcceptanceTicketAndCannotAdministerRabbit() throws Exception {
        when(receipts.findFirstByTrackingCodeAndDomainAndEventTypeAndStatusOrderByProcessedAtDesc(
                "RX-TEST", "TICKET", "shipment.status_changed", "ACEPTADO")).thenReturn(java.util.Optional.empty());
        mvc.perform(get("/api/operator/confirmations/RX-TEST").with(jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_OPERATOR")))).andExpect(status().isNotFound());
        verify(receipts).findFirstByTrackingCodeAndDomainAndEventTypeAndStatusOrderByProcessedAtDesc(
                "RX-TEST", "TICKET", "shipment.status_changed", "ACEPTADO");
        mvc.perform(get("/api/rabbitmq/topology").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/operator/confirmations/INVALID").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                .andExpect(status().isBadRequest());
    }
}
