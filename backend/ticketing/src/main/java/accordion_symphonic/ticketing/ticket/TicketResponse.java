package accordion_symphonic.ticketing.ticket;

public record TicketResponse(
        Long id,
        Long orderId,
        Long ticketCategoryId,
        String ticketCategoryName,
        String qrToken,
        TicketStatus status
) {
    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getOrder().getId(),
                ticket.getTicketCategory().getId(),
                ticket.getTicketCategory().getName(),
                ticket.getQrToken(),
                ticket.getStatus()
        );
    }
}