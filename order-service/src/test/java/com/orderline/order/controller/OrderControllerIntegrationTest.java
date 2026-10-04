package com.orderline.order.controller;

import com.jayway.jsonpath.JsonPath;
import com.orderline.common.event.EventTypes;
import com.orderline.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:4.1.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void createsOrderAndWritesOutboxEvent() throws Exception {
        UUID customerId = UUID.randomUUID();

        String orderId = createOrder(customerId);

        mockMvc.perform(get("/orders/{id}", orderId).with(user(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        assertThat(outboxEventRepository.findAll())
                .anySatisfy(event -> {
                    assertThat(event.getAggregateId()).hasToString(orderId);
                    assertThat(event.getEventType()).isEqualTo(EventTypes.ORDER_CREATED);
                });
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsOrderWithoutItems() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(user(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void hidesOrderOfAnotherCustomer() throws Exception {
        String orderId = createOrder(UUID.randomUUID());

        mockMvc.perform(get("/orders/{id}", orderId).with(user(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void cancelsOrderOnlyOnce() throws Exception {
        UUID customerId = UUID.randomUUID();
        String orderId = createOrder(customerId);

        mockMvc.perform(post("/orders/{id}/cancel", orderId).with(user(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/orders/{id}/cancel", orderId).with(user(customerId)))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotPayOrderBeforeReservation() throws Exception {
        UUID customerId = UUID.randomUUID();
        String orderId = createOrder(customerId);

        mockMvc.perform(post("/orders/{id}/pay", orderId).with(user(customerId)))
                .andExpect(status().isConflict());
    }

    @Test
    void listsOnlyOwnOrders() throws Exception {
        UUID customerId = UUID.randomUUID();
        createOrder(customerId);
        createOrder(customerId);
        createOrder(UUID.randomUUID());

        mockMvc.perform(get("/orders").with(user(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    private String createOrder(UUID customerId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/orders")
                        .with(user(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": "%s", "quantity": 2}]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        String orderId = JsonPath.read(response.getContentAsString(), "$.id");
        assertThat(response.getHeader("Location")).endsWith("/orders/" + orderId);
        return orderId;
    }

    private static RequestPostProcessor user(UUID customerId) {
        return jwt().jwt(token -> token.subject(customerId.toString()).claim("role", "USER"));
    }
}
