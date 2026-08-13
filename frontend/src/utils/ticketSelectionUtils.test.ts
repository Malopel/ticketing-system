import {describe, expect, it} from 'vitest';

import type {TicketCategory}
    from '../api/ticketCategoryApi.ts';

import {clampQuantitiesToAvailability}
    from './ticketSelectionUtils.ts';

describe('clampQuantitiesToAvailability', () => {
    const categories: TicketCategory[] = [
        {
            id: 1,
            name: 'Normal',
            price: 25,
            capacity: 100,
            available: 3,
        },
        {
            id: 2,
            name: 'Ermäßigt',
            price: 15,
            capacity: 50,
            available: 5,
        },
        {
            id: 3,
            name: 'VIP',
            price: 50,
            capacity: 10,
            available: 0,
        },
    ];

    it('limits quantities to current availability', () => {
        const result =
            clampQuantitiesToAvailability(
                {
                    1: 5,
                    2: 2,
                    3: 4,
                },
                categories,
            );

        expect(result).toEqual({
            1: 3,
            2: 2,
            3: 0,
        });
    });

    it('uses zero for categories without a stored quantity', () => {
        const result =
            clampQuantitiesToAvailability(
                {},
                categories,
            );

        expect(result).toEqual({
            1: 0,
            2: 0,
            3: 0,
        });
    });
});