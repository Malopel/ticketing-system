import type { OrderResponse } from './orderApi';
import {throwApiError} from './apiError';

export type PaymentSession = {
    providerPaymentId: string;
    checkoutUrl: string;
};

export async function startPayment(
    concertId: number,
    orderId: number,
    accessToken: string,
): Promise<PaymentSession> {
    const response = await fetch(
        `/api/concerts/${concertId}/orders/${orderId}/payment`,
        {
            method: 'POST',
            headers: {
                'X-Order-Access-Token': accessToken,
            },
        },
    );

    if (!response.ok) {
        await throwApiError(
            response,
            'Zahlung konnte nicht gestartet werden.',
        );
    }

    return response.json();
}

export async function completeFakePayment(
    providerPaymentId: string,
): Promise<OrderResponse> {
    const response = await fetch(
        `/api/fake-payments/${providerPaymentId}/complete`,
        {
            method: 'POST',
        },
    );

    if (!response.ok) {
        await throwApiError(
            response,
            'Fake-Zahlung konnte nicht abgeschlossen werden.',
        );
    }

    return response.json();
}