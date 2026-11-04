import {useState} from 'react';

import type {
    AdminConcertStatus,
} from '../../api/adminConcertApi';

import {useAdminTicketCategories}
    from '../../hooks/useAdminTicketCategories';

import TicketCategoryRow
    from './TicketCategoryRow';

import TicketCategoryCreateForm
    from '../../components/TicketCategoryCreateForm';

import TicketCategoryUpdateForm
    from '../../components/TicketCategoryUpdateForm';

type TicketCategoriesSectionProps = {
    concertId: number;
    concertStatus: AdminConcertStatus;
};

function TicketCategorySection({
                                   concertId,
                                   concertStatus,
                               }: TicketCategoriesSectionProps) {
    const [isCreatingCategory, setIsCreatingCategory] =
        useState(false);

    const [editingCategoryId, setEditingCategoryId] =
        useState<number | null>(null);

    const {
        ticketCategories,
        isLoading: areCategoriesLoading,
        error: categoriesError,
        reload: reloadTicketCategories,
    } = useAdminTicketCategories(concertId);

    const editable = concertStatus === 'DRAFT';

    return (
        <section className="admin-detail-section">
            <div className="admin-section-header">
                <div>
                    <h2>Ticketkategorien</h2>

                    <p>
                        Preise und verfügbare Plätze für dieses
                        Konzert.
                    </p>
                </div>

                {editable && (
                    <button
                        type="button"
                        onClick={() =>
                            setIsCreatingCategory(true)
                        }
                        disabled={isCreatingCategory}
                    >
                        + Ticketkategorie
                    </button>
                )}
            </div>

            {isCreatingCategory && (
                <TicketCategoryCreateForm
                    concertId={concertId}
                    onCancel={() =>
                        setIsCreatingCategory(false)
                    }
                    onCreated={async () => {
                        await reloadTicketCategories();
                        setIsCreatingCategory(false);
                    }}
                />
            )}

            {areCategoriesLoading ? (
                <p>Ticketkategorien werden geladen...</p>
            ) : categoriesError ? (
                <p
                    className="admin-error-message"
                    role="alert"
                >
                    {categoriesError}
                </p>
            ) : ticketCategories.length === 0 ? (
                <div className="admin-empty-state">
                    <h3>Noch keine Ticketkategorien</h3>

                    <p>
                        Für dieses Konzert wurden noch keine
                        Ticketkategorien angelegt.
                    </p>
                </div>
            ) : (
                <div className="admin-ticket-category-list">
                    {ticketCategories.map((category) => (
                        <div
                            key={category.id}
                            className="admin-ticket-category-item"
                        >
                            <TicketCategoryRow
                                category={category}
                                editable={editable}
                                onEdit={() =>
                                    setEditingCategoryId(
                                        category.id,
                                    )
                                }
                            />

                            {editingCategoryId === category.id && (
                                <TicketCategoryUpdateForm
                                    concertId={concertId}
                                    category={category}
                                    onCancel={() =>
                                        setEditingCategoryId(null)
                                    }
                                    onUpdated={async () => {
                                        await reloadTicketCategories();
                                        setEditingCategoryId(null);
                                    }}
                                />
                            )}
                        </div>
                    ))}
                </div>
            )}
        </section>
    );
}

export default TicketCategorySection;