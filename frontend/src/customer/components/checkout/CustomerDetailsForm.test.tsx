import {fireEvent, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {describe, expect, it, vi} from 'vitest';

import CustomerDetailsForm from './CustomerDetailsForm.tsx';

describe('CustomerDetailsForm', () => {
    it('submits the entered email address', async () => {
        const user = userEvent.setup();

        const onSubmit = vi.fn(
            () => Promise.resolve(),
        );

        render(
            <CustomerDetailsForm
                onSubmit={onSubmit}
            />,
        );

        await user.type(
            screen.getByLabelText('E-Mail-Adresse'),
            'kunde@example.de',
        );

        await user.click(
            screen.getByRole('button', {
                name: 'Zahlungspflichtig bestellen',
            }),
        );

        expect(onSubmit).toHaveBeenCalledTimes(1);

        expect(onSubmit).toHaveBeenCalledWith(
            'kunde@example.de',
        );
    });

    it('blocks duplicate submissions', async () => {
        const user = userEvent.setup();

        let resolveSubmit!: () => void;

        const onSubmit = vi.fn(
            () =>
                new Promise<void>((resolve) => {
                    resolveSubmit = resolve;
                }),
        );

        render(
            <CustomerDetailsForm
                onSubmit={onSubmit}
            />,
        );

        await user.type(
            screen.getByLabelText('E-Mail-Adresse'),
            'kunde@example.de',
        );

        const submitButton = screen.getByRole(
            'button',
            {
                name: 'Zahlungspflichtig bestellen',
            },
        );

        const form = submitButton.closest('form');

        if (!form) {
            throw new Error('Formular nicht gefunden');
        }

        fireEvent.submit(form);
        fireEvent.submit(form);

        expect(onSubmit).toHaveBeenCalledTimes(1);

        resolveSubmit();
    });
});