package accordion_symphonic.ticketing.bank;

import accordion_symphonic.ticketing.bank.dto.BankTransferInstructions;
import accordion_symphonic.ticketing.bank.dto.BankTransferProperties;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.*;
import accordion_symphonic.ticketing.order.exception.OrderCannotBePaidException;
import accordion_symphonic.ticketing.order.exception.OrderNotFoundException;
import accordion_symphonic.ticketing.order.service.OrderAccessTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BankTransferService {

    private final BankTransferProperties properties;
    private final OrderRepository orderRepository;
    private final OrderAccessTokenService orderAccessTokenService;

    public BankTransferService(
            BankTransferProperties properties,
            OrderRepository orderRepository,
            OrderAccessTokenService orderAccessTokenService
    ) {
        this.properties = properties;
        this.orderRepository = orderRepository;
        this.orderAccessTokenService = orderAccessTokenService;
    }

    @Transactional(
            noRollbackFor = OrderCannotBePaidException.class
    )
    public BankTransferInstructions startBankTransfer(
            Long concertId,
            Long orderId,
            String accessToken
    ) throws OrderCannotBePaidException {
        if (!properties.enabled()) {
            throw new IllegalStateException(
                    "Bank transfer payments are disabled."
            );
        }

        Order order = orderRepository
                .findByIdAndConcertIdForUpdate(
                        orderId,
                        concertId
                )
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );

        boolean hasAccess =
                orderAccessTokenService.matches(
                        accessToken,
                        order.getAccessTokenHash()
                );

        if (!hasAccess) {
            throw new OrderNotFoundException(orderId);
        }

        if (order.shouldExpire()) {
            order.expire();

            throw new OrderCannotBePaidException(orderId);
        }

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new OrderCannotBePaidException(orderId);
        }

        if (order.getConcert().getStatus()
                != ConcertStatus.PUBLISHED) {
            throw new OrderCannotBePaidException(orderId);
        }

        LocalDateTime paymentExpiresAt =
                LocalDateTime.now()
                        .plusHours(properties.reservationHours());

        order.markAsPaymentPending(
                PaymentMethod.BANK_TRANSFER,
                paymentExpiresAt
        );

        return createInstructions(order);
    }

    public BankTransferInstructions createInstructions(
            Order order
    ) {
        String reference = order.getPublicId();

        String paytoUri = buildPaytoUri(
                properties.iban(),
                properties.recipient(),
                order.getTotalAmount(),
                reference
        );

        String epcPayload = buildEpcPayload(
                properties.recipient(),
                properties.iban(),
                properties.bic(),
                order.getTotalAmount(),
                reference
        );

        return new BankTransferInstructions(
                properties.recipient(),
                properties.iban(),
                properties.bic(),
                order.getTotalAmount(),
                reference,
                order.getPaymentExpiresAt(),
                paytoUri,
                epcPayload
        );
    }

    private String buildPaytoUri(
            String iban,
            String recipient,
            BigDecimal amount,
            String reference
    ) {
        String cleanIban = iban.replace(" ", "");

        return UriComponentsBuilder
                .fromUriString(
                        "payto://iban/" + cleanIban
                )
                .queryParam(
                        "amount",
                        "EUR:" + amount.setScale(2)
                )
                .queryParam(
                        "receiver-name",
                        recipient
                )
                .queryParam(
                        "message",
                        reference
                )
                .build()
                .encode()
                .toUriString();
    }

    private String buildEpcPayload(
            String recipient,
            String iban,
            String bic,
            BigDecimal amount,
            String reference
    ) {
        return String.join(
                "\n",
                "BCD",
                "002",
                "1",
                "SCT",
                bic == null ? "" : bic,
                recipient,
                iban.replace(" ", ""),
                "EUR" + amount.setScale(2),
                "",
                "",
                reference,
                ""
        );
    }
}