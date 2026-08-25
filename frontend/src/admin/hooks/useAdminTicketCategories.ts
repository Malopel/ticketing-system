import {
    useCallback,
    useEffect,
    useState,
} from 'react';

import {
    getAdminTicketCategories,
    type AdminTicketCategory,
} from '../api/adminTicketCategoryApi';

export function useAdminTicketCategories(
    concertId: number | null,
) {
    const [ticketCategories, setTicketCategories] =
        useState<AdminTicketCategory[]>([]);

    const [isLoading, setIsLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    useEffect(() => {
        if (concertId === null) {
            return;
        }

        const currentConcertId = concertId;

        const controller = new AbortController();

        async function loadInitial() {
            try {
                const data =
                    await getAdminTicketCategories(
                        currentConcertId,
                        controller.signal,
                    );

                if (!controller.signal.aborted) {
                    setTicketCategories(data);
                    setError(null);
                }
            } catch (error) {
                if (
                    error instanceof DOMException &&
                    error.name === 'AbortError'
                ) {
                    return;
                }

                if (!controller.signal.aborted) {
                    setError(
                        'Ticketkategorien konnten nicht geladen werden.',
                    );
                }
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoading(false);
                }
            }
        }

        void loadInitial();

        return () => {
            controller.abort();
        };
    }, [concertId]);

    const reload = useCallback(async () => {
        if (concertId === null) {
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const data =
                await getAdminTicketCategories(concertId);

            setTicketCategories(data);
        } catch {
            setError(
                'Ticketkategorien konnten nicht geladen werden.',
            );
        } finally {
            setIsLoading(false);
        }
    }, [concertId]);

    return {
        ticketCategories,
        isLoading,
        error,
        reload,
    };
}