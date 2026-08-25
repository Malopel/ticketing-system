import {
    useCallback,
    useEffect,
    useState,
} from 'react';

import {
    getAdminTicketCategories,
    type AdminTicketCategory,
} from '../api/adminTicketCategoryApi'

export function useAdminTicketCategories(
    concertId: number | null,
) {
    const [ticketCategories, setTicketCategories] =
    useState<AdminTicketCategory[]>([]);

    const [isLoading, setIsLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    const load = useCallback(async (
        signal?: AbortSignal,
    ) => {
        if (concertId === null) {
            return;
        }

        try {
            const data = await getAdminTicketCategories(
                concertId,
                signal,
            );

            setTicketCategories(data);
            setError(null);
        } catch (error) {
            if (
                error instanceof DOMException &&
                error.name === 'AbortError'
            ) {
                return;
            }

            setError(
                'Ticketkategorien konnten nicht geladen werden.',
            );
        } finally {
            if (!signal?.aborted) {
                setIsLoading(false);
            }
        }
    }, [concertId]);

    useEffect(() => {
        const controller = new AbortController();

        void load(controller.signal);

        return () => {
            controller.abort();
        };
    }, [load]);

    const reload = useCallback(async () => {
        setIsLoading(true);

        await load();
    }, [load]);

    return {
        ticketCategories,
        isLoading,
        error,
        reload,
    };
}