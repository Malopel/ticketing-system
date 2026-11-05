import { adminApiClient } from './adminApiClient';

export interface TicketResponse {
    id: number;
    orderId: number;
    ticketCategoryId: number;
    ticketCategoryName: string;
    qrToken: string;
    status: 'VALID' | 'USED';
}

export async function validateTicket(
    concertId: number,
    qrToken: string,
): Promise<TicketResponse> {
    return adminApiClient.request<TicketResponse>(
        `/api/admin/concerts/${concertId}/tickets/validate/${encodeURIComponent(qrToken)}`,
    );
}

export async function markTicketAsUsed(
    concertId: number,
    qrToken: string,
): Promise<TicketResponse> {
    return adminApiClient.request<TicketResponse>(
        `/api/admin/concerts/${concertId}/tickets/${encodeURIComponent(qrToken)}/use`,
        {
            method: 'PATCH',
        },
    );
}