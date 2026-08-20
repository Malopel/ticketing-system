package accordion_symphonic.ticketing.ticket.dto;

import accordion_symphonic.ticketing.order.Order;

import java.util.List;

public record TicketOrderData(
        Order order,
        List<TicketResponse> tickets
) {}
