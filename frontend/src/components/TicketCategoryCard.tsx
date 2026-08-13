import './styles/TicketCategoryCard.css'

import type {TicketCategory} from '../api/ticketCategoryApi';

type TicketCategoryCardProps = {
    category: TicketCategory;
    quantity: number;
    onQuantityChange: (categoryId: number, quantity: number) => void;
    canIncrease: boolean;
};

function TicketCategoryCard({
                                category,
                                quantity,
                                onQuantityChange,
                                canIncrease,
                            }: TicketCategoryCardProps) {
    function handleDecrease() {
        if (quantity > 0) {
            onQuantityChange(category.id, quantity - 1)
        }
    }

    function handleIncrease() {
        if (
            canIncrease &&
            quantity < category.available
        ) {
            onQuantityChange(category.id, quantity + 1)
        }
    }

    const currencyFormatter = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
    });

    const criticalAvailabilityThreshold = Math.min(
        100,
        Math.ceil(category.capacity * 0.25),
    );

    const isLowAvailability =
        category.available <= criticalAvailabilityThreshold;

    return (
        <article className="ticket-category-card">
            <div className="ticket-category-info">
                <h3>{category.name}</h3>

                <p>
                    {currencyFormatter.format(category.price)}
                </p>

                {category.available === 0 ? (
                    <p className="ticket-availability sold-out">
                        Ausverkauft
                    </p>
                ) : isLowAvailability ? (
                    <p className="ticket-availability low">
                        Nur noch {category.available} verfügbar
                    </p>
                ) : (
                    <p className="ticket-availability">
                        Verfügbar
                    </p>
                )}
            </div>

            <div className="quantity-control">
                <button
                    type="button"
                    className="quantity-button"
                    onClick={handleDecrease}
                    disabled={quantity === 0}
                >
                    −
                </button>

                <span className="quantity-value">
                    {quantity}
                </span>

                <button
                    type="button"
                    className="quantity-button"
                    onClick={handleIncrease}
                    disabled={
                        quantity >= category.available ||
                        !canIncrease
                    }
                >
                    +
                </button>
            </div>
        </article>
    );
}

export default TicketCategoryCard;