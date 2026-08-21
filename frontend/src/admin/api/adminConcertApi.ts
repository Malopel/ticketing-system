import {adminApiClient} from './adminApiClient';

export type AdminConcertStatus =
    | 'DRAFT'
    | 'PUBLISHED'
    | 'CANCELLED'
    | 'ARCHIVED';

export type AdminConcert = {
    id: number;
    title: string;
    description: string;
    startTime: string;
    location: string;
    status: AdminConcertStatus;
};

export type AdminConcertRequest = {
    title: string;
    description: string;
    startTime: string;
    location: string;
};

export async function getAdminConcerts(
    signal?: AbortSignal,
): Promise<AdminConcert[]> {
    return adminApiClient.request<AdminConcert[]>(
        '/api/admin/concerts',
        {
            signal,
        },
    );
}

export async function createAdminConcert(
    request: AdminConcertRequest,
): Promise<AdminConcert> {
    return adminApiClient.request<AdminConcert>(
        '/api/admin/concerts',
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        },
    );
}