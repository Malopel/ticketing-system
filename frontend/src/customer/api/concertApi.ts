import {throwApiError} from './apiError.ts';

export type Concert = {
    id: number;
    title: string;
    description: string;
    startTime: string;
    location: string;
    status: PublicConcertStatus;
};

export type PublicConcertStatus =
    | 'PUBLISHED'
    | 'CANCELLED';

export async function getConcerts(): Promise<Concert[]> {
    const response = await fetch('/api/concerts');

    if (!response.ok) {
        await throwApiError(
            response,
            'Konzerte konnten nicht geladen werden.',
        );
    }

    return response.json();
}

export async function getConcertById(concertId: number): Promise<Concert> {
    const response = await fetch(`/api/concerts/${concertId}`);

    if (!response.ok) {
        await throwApiError(
            response,
            'Konzert konnte nicht geladen werden.',
        );
    }

    return response.json();
}