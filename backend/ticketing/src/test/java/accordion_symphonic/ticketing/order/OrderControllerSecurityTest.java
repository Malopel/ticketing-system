package accordion_symphonic.ticketing.order;

import accordion_symphonic.ticketing.common.ErrorCode;
import accordion_symphonic.ticketing.order.dto.CreatedOrderResponse;
import accordion_symphonic.ticketing.order.dto.OrderRequest;
import accordion_symphonic.ticketing.order.dto.OrderResponse;
import accordion_symphonic.ticketing.order.exception.DuplicateTicketCategoryException;
import accordion_symphonic.ticketing.order.exception.OrderExceptionHandler;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.order.exception.TooManyTicketsInOrderException;
import accordion_symphonic.ticketing.order.service.CustomerOrderService;
import accordion_symphonic.ticketing.order.service.OrderCreationService;
import accordion_symphonic.ticketing.order.web.OrderController;
import accordion_symphonic.ticketing.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = OrderController.class)
@Import({
        SecurityConfig.class,
        OrderExceptionHandler.class
})
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password"
})
class OrderControllerSecurityTest {

    private static final Long CONCERT_ID = 1L;
    private static final Long ORDER_ID = 42L;
    private static final String VALID_TOKEN = "valid-order-access-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerOrderService customerOrderService;
    @MockitoBean
    private OrderCreationService orderCreationService;

    @Test
    void customerCanReadOrderWithAccessToken() throws Exception {
        when(customerOrderService.getCustomerOrder(CONCERT_ID, ORDER_ID, VALID_TOKEN))
                .thenReturn(reservedOrderResponse());

        mockMvc.perform(get("/api/concerts/{concertId}/orders/{orderId}",
                        CONCERT_ID,
                        ORDER_ID)
                        .header("X-Order-Access-Token", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.customerEmail").value("kunde@example.com"))
                .andExpect(jsonPath("$.status").value("RESERVED"));

        verify(customerOrderService).getCustomerOrder(
                CONCERT_ID,
                ORDER_ID,
                VALID_TOKEN
        );
    }

    @Test
    void customerWithoutAccessTokenReceivesNotFoundInsteadOfUnauthorized() throws Exception {
        when(customerOrderService.getCustomerOrder(CONCERT_ID, ORDER_ID, null))
                .thenThrow(new OrderNotFoundException(ORDER_ID));

        mockMvc.perform(get("/api/concerts/{concertId}/orders/{orderId}",
                        CONCERT_ID,
                        ORDER_ID))
                .andExpect(status().isNotFound());

        verify(customerOrderService).getCustomerOrder(
                CONCERT_ID,
                ORDER_ID,
                null
        );
    }

    @Test
    void customerCanCancelOrderWithAccessToken() throws Exception {
        when(customerOrderService.cancelOrder(CONCERT_ID, ORDER_ID, VALID_TOKEN))
                .thenReturn(cancelledOrderResponse());

        mockMvc.perform(patch("/api/concerts/{concertId}/orders/{orderId}/cancel",
                        CONCERT_ID,
                        ORDER_ID)
                        .header("X-Order-Access-Token", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(customerOrderService).cancelOrder(
                CONCERT_ID,
                ORDER_ID,
                VALID_TOKEN
        );
    }

    @Test
    void customerWithWrongAccessTokenCannotCancelOrder() throws Exception {
        String wrongToken = "wrong-token";

        when(customerOrderService.cancelOrder(CONCERT_ID, ORDER_ID, wrongToken))
                .thenThrow(new OrderNotFoundException(ORDER_ID));

        mockMvc.perform(patch("/api/concerts/{concertId}/orders/{orderId}/cancel",
                        CONCERT_ID,
                        ORDER_ID)
                        .header("X-Order-Access-Token", wrongToken))
                .andExpect(status().isNotFound());

        verify(customerOrderService).cancelOrder(
                CONCERT_ID,
                ORDER_ID,
                wrongToken
        );
    }

    private OrderResponse reservedOrderResponse() {
        return new OrderResponse(
                ORDER_ID,
                CONCERT_ID,
                "Testkonzert",
                "kunde@example.com",
                OrderStatus.RESERVED,
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                List.of()
        );
    }

    private OrderResponse cancelledOrderResponse() {
        return new OrderResponse(
                ORDER_ID,
                CONCERT_ID,
                "Testkonzert",
                "kunde@example.com",
                OrderStatus.CANCELLED,
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null,
                List.of()
        );
    }

    @Test
    void customerCanCreateOrderWithoutLoginAndReceivesAccessToken() throws Exception {
        String returnedAccessToken = "new-secret-order-access-token";

        CreatedOrderResponse createdOrderResponse = new CreatedOrderResponse(
                reservedOrderResponse(),
                returnedAccessToken
        );

        when(orderCreationService.createOrder(
                org.mockito.ArgumentMatchers.eq(CONCERT_ID),
                org.mockito.ArgumentMatchers.any(OrderRequest.class)
        )).thenReturn(createdOrderResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/concerts/{concertId}/orders", CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerEmail": "kunde@example.com",
                              "items": [
                                {
                                  "ticketCategoryId": 7,
                                  "quantity": 2
                                }
                              ]
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(ORDER_ID))
                .andExpect(jsonPath("$.order.customerEmail").value("kunde@example.com"))
                .andExpect(jsonPath("$.order.status").value("RESERVED"))
                .andExpect(jsonPath("$.accessToken").value(returnedAccessToken));

        ArgumentCaptor<OrderRequest> requestCaptor =
                ArgumentCaptor.forClass(OrderRequest.class);

        verify(orderCreationService).createOrder(
                org.mockito.ArgumentMatchers.eq(CONCERT_ID),
                requestCaptor.capture()
        );

        OrderRequest capturedRequest = requestCaptor.getValue();

        assertEquals("kunde@example.com", capturedRequest.customerEmail());
        assertEquals(1, capturedRequest.items().size());
        assertEquals(7L, capturedRequest.items().getFirst().ticketCategoryId());
        assertEquals(2, capturedRequest.items().getFirst().quantity());
    }

    @Test
    void createOrderWithInvalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/concerts/{concertId}/orders", CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "customerEmail": "keine-email",
                          "items": [
                            {
                              "ticketCategoryId": 7,
                              "quantity": 2
                            }
                          ]
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void createOrderWithEmptyItemsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/concerts/{concertId}/orders", CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "customerEmail": "kunde@example.com",
                          "items": []
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void createOrderWithQuantityZeroReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/concerts/{concertId}/orders", CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "customerEmail": "kunde@example.com",
                          "items": [
                            {
                              "ticketCategoryId": 7,
                              "quantity": 0
                            }
                          ]
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerOrderService);
    }

    @Test
    void createOrderWithTooManyTicketsReturnsConflict() throws Exception {
        when(orderCreationService.createOrder(eq(CONCERT_ID), any(OrderRequest.class)))
                .thenThrow(new TooManyTicketsInOrderException(11, 10));

        mockMvc.perform(post("/api/concerts/{concertId}/orders", CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "customerEmail": "kunde@example.com",
                          "items": [
                            {
                              "ticketCategoryId": 7,
                              "quantity": 6
                            },
                            {
                              "ticketCategoryId": 8,
                              "quantity": 5
                            }
                          ]
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.TOO_MANY_TICKETS_IN_ORDER))
                .andExpect(jsonPath("$.message").value(
                        "Too many tickets in order: requested 11, maximum allowed is 10"
                ));
    }

    @Test
    void createOrderWithDuplicateTicketCategoryReturnsConflict() throws Exception {
        when(orderCreationService.createOrder(eq(CONCERT_ID), any(OrderRequest.class)))
                .thenThrow(new DuplicateTicketCategoryException(7L));

        mockMvc.perform(post("/api/concerts/{concertId}/orders", CONCERT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "customerEmail": "kunde@example.com",
                          "items": [
                            {
                              "ticketCategoryId": 7,
                              "quantity": 2
                            },
                            {
                              "ticketCategoryId": 7,
                              "quantity": 3
                            }
                          ]
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.DUPLICATE_TICKET_CATEGORY));
    }
}