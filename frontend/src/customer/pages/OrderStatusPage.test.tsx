import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {act, render, screen} from '@testing-library/react';
import {
    MemoryRouter,
    Route,
    Routes,
} from 'react-router-dom';

import OrderStatusPage from './OrderStatusPage.tsx';
import {
    getOrder,
    type OrderResponse,
} from '../api/orderApi.ts';

vi.mock('../api/orderApi', async () => {
    const actual =
        await vi.importActual<typeof import('../api/orderApi.ts')>(
            '../api/orderApi',
        );

    return {
        ...actual,
        getOrder: vi.fn(),
    };
});

vi.mock('../hooks/useOrderCountdown', () => ({
    useOrderCountdown: vi.fn(() => 300),
}));

const mockedGetOrder = vi.mocked(getOrder);

const paymentPendingOrder: OrderResponse = {
    id: 42,
    concertId: 7,
    concertTitle: 'Accordion Night',
    customerEmail: 'kunde@example.com',
    status: 'PAYMENT_PENDING',
    totalAmount: 25,
    createdAt: '2026-08-20T18:00:00',
    expiresAt: '2026-08-20T18:30:00',
    paymentExpiresAt: '2026-08-20T18:20:00',
    paidAt: null,
    items: [
        {
            id: 1,
            ticketCategoryId: 3,
            ticketCategoryName: 'Normalpreis',
            quantity: 1,
            unitPrice: 25,
            totalPrice: 25,
        },
    ],
};

const paidOrder: OrderResponse = {
    ...paymentPendingOrder,
    status: 'PAID',
    paymentExpiresAt: null,
    paidAt: '2026-08-20T18:05:00',
};

describe('OrderStatusPage polling', () => {
    beforeEach(() => {
        vi.useFakeTimers();

        sessionStorage.clear();

        sessionStorage.setItem(
            'order-42',
            JSON.stringify({
                concertId: 7,
                accessToken: 'test-token',
            }),
        );

        mockedGetOrder.mockReset();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('polls a payment pending order until it becomes paid', async () => {
        mockedGetOrder
            .mockResolvedValueOnce(paymentPendingOrder)
            .mockResolvedValueOnce(paidOrder);

        render(
            <MemoryRouter initialEntries={['/orders/42']}>
                <Routes>
                    <Route
                        path="/orders/:orderId"
                        element={<OrderStatusPage/>}
                    />
                </Routes>
            </MemoryRouter>,
        );

        // Initialen getOrder()-Aufruf + React-State-Update
        // abarbeiten lassen.
        await act(async () => {
            await Promise.resolve();
        });

        expect(mockedGetOrder)
            .toHaveBeenCalledTimes(1);

        expect(mockedGetOrder)
            .toHaveBeenLastCalledWith(
                7,
                42,
                'test-token',
            );

        expect(
            screen.getByText(
                /Deine Zahlung wird verarbeitet/,
            ),
        ).toBeInTheDocument();

        // Ersten Poll nach 2,5 Sekunden auslösen.
        await act(async () => {
            await vi.advanceTimersByTimeAsync(2500);
        });

        expect(mockedGetOrder)
            .toHaveBeenCalledTimes(2);

        expect(
            screen.getByText(
                'Deine Bestellung wurde erfolgreich bezahlt.',
            ),
        ).toBeInTheDocument();

        // PAID muss das Polling beendet haben.
        await act(async () => {
            await vi.advanceTimersByTimeAsync(10_000);
        });

        expect(mockedGetOrder)
            .toHaveBeenCalledTimes(2);
    });
});