type BackendErrorResponse = {
    timestamp: string;
    status: number;
    error: string;
    code: string;
    message: string;
};

export class ApiError extends Error {
    readonly status: number;
    readonly code: string;

    constructor(
        message: string,
        status: number,
        code: string,
    ) {
        super(message);

        this.name = 'ApiError';
        this.status = status;
        this.code = code;
    }
}

function isBackendErrorResponse(
    value: unknown,
): value is BackendErrorResponse {
    if (
        typeof value !== 'object' ||
        value === null
    ) {
        return false;
    }

    const error = value as Record<string, unknown>;

    return (
        typeof error.status === 'number' &&
        typeof error.code === 'string' &&
        typeof error.message === 'string'
    );
}

function getUserMessage(
    code: string,
    fallbackMessage: string,
): string {
    switch (code) {
        case 'VALIDATION_ERROR':
            return 'Bitte überprüfe deine Eingaben.';

        case 'CONCERT_NOT_FOUND':
            return 'Dieses Konzert ist nicht mehr verfügbar.';

        case 'TICKET_CATEGORY_NOT_FOUND':
            return 'Eine ausgewählte Ticketkategorie ist nicht mehr verfügbar.';

        case 'ORDER_NOT_FOUND':
            return 'Die Bestellung konnte nicht gefunden oder geöffnet werden.';

        case 'ORDER_CANNOT_BE_PAID':
            return 'Diese Bestellung kann nicht mehr bezahlt werden.';

        case 'NOT_ENOUGH_TICKETS_AVAILABLE':
            return 'Für deine Auswahl sind nicht mehr genügend Tickets verfügbar. Bitte passe die Menge an.';

        case 'TOO_MANY_TICKETS_IN_ORDER':
            return 'Du hast mehr Tickets ausgewählt als pro Bestellung erlaubt.';

        case 'DUPLICATE_TICKET_CATEGORY':
            return 'Eine Ticketkategorie wurde mehrfach übermittelt. Bitte wähle deine Tickets erneut aus.';

        case 'ORDER_HAS_NO_TICKETS':
            return 'Für diese Bestellung sind keine Tickets vorhanden.';

        default:
            return fallbackMessage;
    }
}

export async function throwApiError(
    response: Response,
    fallbackMessage: string,
): Promise<never> {
    let body: unknown;

    try {
        body = await response.json();
    } catch {
        throw new ApiError(
            fallbackMessage,
            response.status,
            'UNKNOWN_ERROR',
        );
    }

    if (!isBackendErrorResponse(body)) {
        throw new ApiError(
            fallbackMessage,
            response.status,
            'UNKNOWN_ERROR',
        );
    }

    throw new ApiError(
        getUserMessage(
            body.code,
            fallbackMessage,
        ),
        body.status,
        body.code,
    );
}