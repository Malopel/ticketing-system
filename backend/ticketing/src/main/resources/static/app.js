let selectedConcert = null;
let selectedTicketCategory = null;

const loadConcertsButton = document.getElementById("loadConcertsButton");
const concertsContainer = document.getElementById("concerts");
const ticketCategorySection = document.getElementById("ticketCategorySection");
const ticketCategoriesContainer = document.getElementById("ticketCategories");
const orderSection = document.getElementById("orderSection");
const createOrderButton = document.getElementById("createOrderButton");
const resultSection = document.getElementById("resultSection");
const errorSection = document.getElementById("errorSection");
const errorMessage = document.getElementById("errorMessage");

loadConcertsButton.addEventListener("click", loadConcerts);
createOrderButton.addEventListener("click", createOrder);

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
                <p><strong>Verfügbare Plätze:</strong> ${category.availableTickets ?? "unbekannt"}</p>
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
            throw new Error("Bestellung konnte nicht erstellt werden.");
        }

        const createdOrder = await response.json();

        document.getElementById("orderId").textContent = createdOrder.order.id;
        document.getElementById("accessToken").textContent = createdOrder.accessToken;

        resultSection.classList.remove("hidden");
    } catch (error) {
        showError(error.message);
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