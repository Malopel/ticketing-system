import './styles/TicketSelection.css'

import {useState} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';

import type {TicketCategory} from '../api/ticketCategoryApi';
import {clampQuantitiesToAvailability} from '../utils/ticketSelectionUtils.ts';
import TicketCategoryCard from './TicketCategoryCard';

type TicketSelectionProps = {
    concertId: number;
    ticketCategories: TicketCategory[];
};

function TicketSelection({
                             concertId,
                             ticketCategories,
                         }: TicketSelectionProps) {
    const navigate = useNavigate();

    const [quantities, setQuantities] =
        useState<Record<number, number>>(() => {
            const storedQuantities = sessionStorage.getItem(
                `checkout-${concertId}`,
            );

            if (!storedQuantities) {
                return {};
            }

            try {
                const parsedQuantities =
                    JSON.parse(storedQuantities) as Record<
                        number,
                        number
                    >;

                return clampQuantitiesToAvailability(
                    parsedQuantities,
                    ticketCategories
                );
            } catch {
                sessionStorage.removeItem(
                    `checkout-${concertId}`,
                );

                return {};
            }
        });

    const currencyFormatter = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
    });

    function handleQuantityChange(
        categoryId: number,
        quantity: number,
    ) {
        setQuantities((currentQuantities) => ({
            ...currentQuantities,
            [categoryId]: quantity,
        }));
    }

    const totalQuantity = Object.values(quantities).reduce(
        (sum, quantity) => sum + quantity,
        0,
    );

    const totalPrice = ticketCategories.reduce(
        (sum, category) => {
            const quantity = quantities[category.id] ?? 0;

            return sum + category.price * quantity;
        },
        0,
    );

    const location = useLocation();

    const availabilityChanged =
        (
            location.state as {
                availabilityChanged?: boolean;
            } | null
        )?.availabilityChanged === true;

    function handleContinueToCheckout() {
        if (totalQuantity === 0) {
            return;
        }

        sessionStorage.setItem(
            `checkout-${concertId}`,
            JSON.stringify(quantities),
        );

        navigate(`/concerts/${concertId}/checkout`);
    }

    return (
        <section className="ticket-selection">
            <div className="ticket-selection-header">
                <h2>Tickets auswählen</h2>

                {availabilityChanged && (
                    <p className="availability-warning">
                        Die Ticketverfügbarkeit hat sich geändert.
                        Wir haben deine Auswahl an den aktuellen
                        Bestand angepasst. Bitte prüfe sie noch einmal.
                    </p>
                )}

                <p>
                    Wähle deine gewünschten Ticketkategorien
                    und Mengen aus.
                </p>
            </div>

            {ticketCategories.length === 0 && (
                <p>
                    Für dieses Konzert sind aktuell keine Tickets verfügbar.
                </p>
            )}

            <div className="ticket-list">
                {ticketCategories.map((category) => (
                    <TicketCategoryCard
                        key={category.id}
                        category={category}
                        quantity={quantities[category.id] ?? 0}
                        onQuantityChange={handleQuantityChange}
                    />
                ))}
            </div>

            {totalQuantity > 0 && (
                <div className={"ticket-summary"}>
                    <div>
                        <strong>Ausgewählte Tickets:</strong>{' '}
                        <span>{totalQuantity}</span>
                    </div>

                    <div>
                        <strong>Gesamtpreis:</strong>{' '}
                        <span>{currencyFormatter.format(totalPrice)}</span>
                    </div>

                    <button
                        type="button"
                        onClick={handleContinueToCheckout}
                    >
                        Weiter zur Bestellung
                    </button>
                </div>
            )}
        </section>
    );
}

export default TicketSelection;