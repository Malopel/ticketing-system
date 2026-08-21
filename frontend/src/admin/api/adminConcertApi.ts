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