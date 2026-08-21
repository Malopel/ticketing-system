import {throwApiError} from './apiError.ts';

export type TicketCategory = {
    id: number;
    name: string;
    price: number;
    capacity: number;
    available: number;
};

export async function getTicketCategories(
    concertId: number,
): Promise<TicketCategory[]> {
    const response = await fetch(
        `/api/concerts/${concertId}/ticket-categories`,
    );

    if (!response.ok) {
        await throwApiError(
            response,
            'Ticketkategorien konnten nicht geladen werden.',
        );
    }

    return response.json();
}