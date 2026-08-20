import {useEffect, useState} from 'react';

import type {OrderResponse} from '../api/orderApi';

function calculateRemainingSeconds(
    deadline: string,
): number {
    const remainingMilliseconds =
        new Date(deadline).getTime() - Date.now();

    return Math.max(
        0,
        Math.ceil(remainingMilliseconds / 1000),
    );
}

function getOrderDeadline(
    order: OrderResponse,
): string | null {
    switch (order.status) {
        case 'RESERVED':
            return order.expiresAt;

        case 'PAYMENT_PENDING':
            return order.paymentExpiresAt;

        default:
            return null;
    }
}

export function useOrderCountdown(
    order: OrderResponse | null,
): number | null {
    const deadline =
        order ? getOrderDeadline(order) : null;

    const countdownKey =
        order && deadline
            ? `${order.id}:${order.status}:${deadline}`
            : null;

    const [countdown, setCountdown] = useState<{
        key: string;
        seconds: number;
    } | null>(null);

    useEffect(() => {
        if (!countdownKey || !deadline) {
            return;
        }

        const currentKey = countdownKey;
        const currentDeadline = deadline;

        const intervalId = window.setInterval(() => {
            const seconds =
                calculateRemainingSeconds(
                    currentDeadline,
                );

            setCountdown({
                key: currentKey,
                seconds,
            });

            if (seconds === 0) {
                window.clearInterval(intervalId);
            }
        }, 1000);

        return () => {
            window.clearInterval(intervalId);
        };
    }, [countdownKey, deadline]);

    if (!deadline || !countdownKey) {
        return null;
    }

    if (countdown?.key === countdownKey) {
        return countdown.seconds;
    }

    return calculateRemainingSeconds(deadline);
}