import {throwApiError} from './apiError';

export type OrderItemRequest = {
    ticketCategoryId: number;
    quantity: number;
};

export type OrderRequest = {
    customerEmail: string;
    items: OrderItemRequest[];
};

export type OrderItemResponse = {
    id: number;
    ticketCategoryId: number;
    ticketCategoryName: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
};

export type OrderStatus =
    | 'RESERVED'
    | 'PAID'
    | 'EXPIRED'
    | 'CANCELLED';

export type OrderResponse = {
    id: number;
    concertId: number;
    concertTitle: string;
    customerEmail: string;
    status: OrderStatus;
    totalAmount: number;
    createdAt: string;
    expiresAt: string;
    paidAt: string | null;
    items: OrderItemResponse[];
};

export type CreatedOrderResponse = {
    order: OrderResponse;
    accessToken: string;
};

export async function createOrder(
    concertId: number,
    orderRequest: OrderRequest,
): Promise<CreatedOrderResponse> {
    const response = await fetch(`/api/concerts/${concertId}/orders`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderRequest),
    });

    if (!response.ok) {
        await throwApiError(
            response,
            'Bestellung konnte nicht erstellt werden.',
        );
    }

    return response.json();
}

export async function getOrder(
    concertId: number,
    orderId: number,
    accessToken: string,
): Promise<OrderResponse> {
    const response = await fetch(
        `/api/concerts/${concertId}/orders/${orderId}`,
        {
            headers: {
                'X-Order-Access-Token': accessToken,
            },
        },
    );

    if (!response.ok) {
        await throwApiError(
            response,
            'Bestellung konnte nicht geladen werden.',
        );
    }

    return response.json();
}