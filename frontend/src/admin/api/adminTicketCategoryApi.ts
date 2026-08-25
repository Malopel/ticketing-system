import {adminApiClient} from './adminApiClient';

export type AdminTicketCategory = {
    id: number;
    name: string;
    price: number;
    capacity: number;
    available: number;
};

export type AdminTicketCategoryRequest = {
    name: string;
    price: number;
    capacity: number;
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

export async function createAdminTicketCategory(
    concertId: number,
    request: AdminTicketCategoryRequest,
): Promise<AdminTicketCategory> {
    return adminApiClient.request<AdminTicketCategory>(
        `/api/admin/concerts/${concertId}/ticket-categories`,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        }
    )
}
