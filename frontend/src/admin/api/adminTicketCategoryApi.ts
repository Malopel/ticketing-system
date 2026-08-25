import {adminApiClient} from './adminApiClient';

export type AdminTicketCategory = {
    id: number;
    name: string;
    price: number;
    capacity: number;
    available: number;
};

export async function getAdminTicketCategories(
    concertId: number,
    signal?: AbortSignal,
): Promise<AdminTicketCategory[]> {
    return adminApiClient.request<AdminTicketCategory[]>(
        `/api/admin/concerts/${concertId}/ticket-categories`,
        {
            signal,
        },
    );
}