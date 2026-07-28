let selectedConcert = null;
let selectedTicketCategory = null;
let currentOrder = null;
let currentAccessToken = null;

const loadConcertsButton = document.getElementById("loadConcertsButton");
const concertsContainer = document.getElementById("concerts");
const ticketCategorySection = document.getElementById("ticketCategorySection");
const ticketCategoriesContainer = document.getElementById("ticketCategories");
const orderSection = document.getElementById("orderSection");
const createOrderButton = document.getElementById("createOrderButton");
const resultSection = document.getElementById("resultSection");
const errorSection = document.getElementById("errorSection");
const errorMessage = document.getElementById("errorMessage");

const refreshOrderButton = document.getElementById("refreshOrderButton");
const cancelOrderButton = document.getElementById("cancelOrderButton");

loadConcertsButton.addEventListener("click", loadConcerts);
createOrderButton.addEventListener("click", createOrder);
refreshOrderButton.addEventListener("click", refreshCurrentOrder);
cancelOrderButton.addEventListener("click", cancelCurrentOrder);

async function loadConcerts() {
    hideError();

    try {
        const response = await fetch("/api/concerts");

        if (!response.ok) {
            throw new Error("Konzerte konnten nicht geladen werden.");
        }

        const concerts = await response.json();

        concertsContainer.innerHTML = "";

        if (concerts.length === 0) {
            concertsContainer.innerHTML = "<p>Aktuell gibt es keine veröffentlichten Konzerte.</p>";
            return;
        }

        concerts.forEach(concert => {
            const element = document.createElement("div");
            element.className = "item";

            element.innerHTML = `
                <h3>${concert.title}</h3>
                <p><strong>Ort:</strong> ${concert.location}</p>
                <p><strong>Beginn:</strong> ${formatDate(concert.startTime)}</p>
                <p>${concert.description ?? ""}</p>
            `;

            element.addEventListener("click", () => selectConcert(concert, element));

            concertsContainer.appendChild(element);
        });
    } catch (error) {
        showError(error.message);
    }
}

async function selectConcert(concert, element) {
    hideError();

    selectedConcert = concert;
    selectedTicketCategory = null;
    currentOrder = null;
    currentAccessToken = null;

    document.querySelectorAll("#concerts .item")
        .forEach(item => item.classList.remove("selected"));

    element.classList.add("selected");

    await loadTicketCategories(concert.id);
}

async function loadTicketCategories(concertId) {
    try {
        const response = await fetch(`/api/concerts/${concertId}/ticket-categories`);

        if (!response.ok) {
            throw new Error("Ticketkategorien konnten nicht geladen werden.");
        }

        const categories = await response.json();

        ticketCategoriesContainer.innerHTML = "";
        ticketCategorySection.classList.remove("hidden");
        orderSection.classList.add("hidden");
        resultSection.classList.add("hidden");

        if (categories.length === 0) {
            ticketCategoriesContainer.innerHTML = "<p>Für dieses Konzert gibt es noch keine Ticketkategorien.</p>";
            return;
        }

        categories.forEach(category => {
            const element = document.createElement("div");
            element.className = "item";

            element.innerHTML = `
                <h3>${category.name}</h3>
                <p><strong>Preis:</strong> ${formatPrice(category.price)}</p>
                <p><strong>Verfügbare Plätze:</strong> ${category.available ?? "unbekannt"}</p>
            `;

            element.addEventListener("click", () => selectTicketCategory(category, element));

            ticketCategoriesContainer.appendChild(element);
        });
    } catch (error) {
        showError(error.message);
    }
}

function selectTicketCategory(category, element) {
    selectedTicketCategory = category;

    document.querySelectorAll("#ticketCategories .item")
        .forEach(item => item.classList.remove("selected"));

    element.classList.add("selected");

    orderSection.classList.remove("hidden");
    resultSection.classList.add("hidden");
}

async function createOrder() {
    hideError();

    if (!selectedConcert || !selectedTicketCategory) {
        showError("Bitte zuerst Konzert und Ticketkategorie auswählen.");
        return;
    }

    const customerEmail = document.getElementById("customerEmail").value;
    const quantity = Number(document.getElementById("quantity").value);

    if (!customerEmail) {
        showError("Bitte eine E-Mail-Adresse eingeben.");
        return;
    }

    if (!quantity || quantity < 1) {
        showError("Bitte eine gültige Ticketanzahl eingeben.");
        return;
    }

    const requestBody = {
        customerEmail: customerEmail,
        items: [
            {
                ticketCategoryId: selectedTicketCategory.id,
                quantity: quantity
            }
        ]
    };

    try {
        const response = await fetch(`/api/concerts/${selectedConcert.id}/orders`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error(await readApiErrorMessage(response));
        }

        const createdOrder = await response.json();

        currentOrder = createdOrder.order;
        currentAccessToken = createdOrder.accessToken;

        renderOrder(currentOrder, currentAccessToken);

        resultSection.classList.remove("hidden");
    } catch (error) {
        showError(error.message);
    }
}

async function refreshCurrentOrder() {
    hideError();

    if (!currentOrder || !currentAccessToken) {
        showError("Es gibt aktuell keine Bestellung zum Aktualisieren.");
        return;
    }

    try {
        const response = await fetch(
            `/api/concerts/${currentOrder.concertId}/orders/${currentOrder.id}`,
            {
                headers: {
                    "X-Order-Access-Token": currentAccessToken
                }
            }
        );

        if (!response.ok) {
            throw new Error(await readApiErrorMessage(response));
        }

        currentOrder = await response.json();

        renderOrder(currentOrder, currentAccessToken);
    } catch (error) {
        showError(error.message);
    }
}

async function cancelCurrentOrder() {
    hideError();

    if (!currentOrder || !currentAccessToken) {
        showError("Es gibt aktuell keine Bestellung zum Stornieren.");
        return;
    }

    try {
        const response = await fetch(
            `/api/concerts/${currentOrder.concertId}/orders/${currentOrder.id}/cancel`,
            {
                method: "PATCH",
                headers: {
                    "X-Order-Access-Token": currentAccessToken
                }
            }
        );

        if (!response.ok) {
            throw new Error(await readApiErrorMessage(response));
        }

        currentOrder = await response.json();

        renderOrder(currentOrder, currentAccessToken);
    } catch (error) {
        showError(error.message);
    }
}

function renderOrder(order, accessToken) {
    document.getElementById("orderId").textContent = order.id;
    document.getElementById("orderStatus").innerHTML = formatOrderStatus(order.status);
    document.getElementById("orderExpiresAt").textContent = formatDate(order.expiresAt);
    document.getElementById("orderTotalAmount").textContent = formatPrice(order.totalAmount);
    document.getElementById("accessToken").textContent = accessToken;

    const orderItems = document.getElementById("orderItems");
    orderItems.innerHTML = "";

    order.items.forEach(item => {
        const element = document.createElement("li");
        element.textContent = `${item.quantity}× ${item.ticketCategoryName} zu ${formatPrice(item.unitPrice)} = ${formatPrice(item.totalPrice)}`;
        orderItems.appendChild(element);
    });
}

async function readApiErrorMessage(response) {
    try {
        const error = await response.json();

        if (error.code) {
            return mapApiErrorCodeToMessage(error.code, error.message);
        }

        if (error.message) {
            return error.message;
        }
    } catch {
        // Response war kein JSON. Dann nutzen wir den Fallback unten.
    }

    return "Ein unbekannter Fehler ist aufgetreten.";
}

function mapApiErrorCodeToMessage(code, fallbackMessage) {
    switch (code) {
        case "TOO_MANY_TICKETS_IN_ORDER":
            return "Du kannst pro Bestellung nur eine begrenzte Anzahl Tickets reservieren.";
        case "DUPLICATE_TICKET_CATEGORY":
            return "Diese Ticketkategorie wurde doppelt ausgewählt.";
        case "NOT_ENOUGH_TICKETS_AVAILABLE":
            return "Für diese Kategorie sind nicht mehr genug Tickets verfügbar.";
        case "ORDER_ACCESS_DENIED":
            return "Du hast keinen Zugriff auf diese Bestellung.";
        case "ORDER_ALREADY_FINALIZED":
            return "Diese Bestellung kann nicht mehr geändert werden.";
        default:
            return fallbackMessage ?? "Ein unbekannter Fehler ist aufgetreten.";
    }
}

function showError(message) {
    errorMessage.textContent = message;
    errorSection.classList.remove("hidden");
}

function hideError() {
    errorSection.classList.add("hidden");
    errorMessage.textContent = "";
}

function formatDate(value) {
    if (!value) {
        return "unbekannt";
    }

    return new Date(value).toLocaleString("de-DE");
}

function formatPrice(value) {
    if (value === undefined || value === null) {
        return "unbekannt";
    }

    return Number(value).toLocaleString("de-DE", {
        style: "currency",
        currency: "EUR"
    });
}

function formatOrderStatus(status) {
    const normalizedStatus = String(status).toLowerCase();

    const label = {
        reserved: "Reserviert",
        paid: "Bezahlt",
        expired: "Abgelaufen",
        cancelled: "Storniert"
    }[normalizedStatus] ?? status;

    return `<span class="status-badge ${normalizedStatus}">${label}</span>`;
}