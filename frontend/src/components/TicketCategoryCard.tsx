import './styles/TicketCategoryCard.css'

import type {TicketCategory} from '../api/ticketCategoryApi';

type TicketCategoryCardProps = {
    category: TicketCategory;
    quantity: number;
    onQuantityChange: (categoryId: number, quantity: number) => void;
};

function TicketCategoryCard({
                                category,
                                quantity,
                                onQuantityChange,
                            }: TicketCategoryCardProps) {
    function handleDecrease() {
        if (quantity > 0) {
            onQuantityChange(category.id, quantity - 1)
        }
    }

    function handleIncrease() {
        if (quantity < category.available) {
            onQuantityChange(category.id, quantity + 1)
        }
    }

    const currencyFormatter = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
    });

    return (
        <article className="ticket-category-card">
            <div className="ticket-category-info">
                <h3>{category.name}</h3>

                <p>
                    {currencyFormatter.format(category.price)}
                </p>

                <small>
                    Noch {category.available} Tickets verfügbar
                </small>
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
                    disabled={quantity >= category.available}
                >
                    +
                </button>
            </div>
        </article>
    );
}

export default TicketCategoryCard;