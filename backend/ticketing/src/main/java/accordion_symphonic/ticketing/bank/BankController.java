package accordion_symphonic.ticketing.bank;

import accordion_symphonic.ticketing.bank.dto.BankTransferInstructions;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.PaymentMethod;
import accordion_symphonic.ticketing.order.exception.OrderCannotBePaidException;
import accordion_symphonic.ticketing.order.service.CustomerOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class BankController {

    private static final String ORDER_ACCESS_TOKEN_HEADER =
            "X-Order-Access-Token";

    private final CustomerOrderService orderService;
    private final BankTransferService bankTransferService;

    public BankController(
            CustomerOrderService orderService,
            BankTransferService bankTransferService
    ) {
        this.orderService = orderService;
        this.bankTransferService = bankTransferService;
    }

    @PostMapping(
            "/api/concerts/{concertId}/orders/{orderId}/bank-transfer"
    )
    public BankTransferInstructions startBankTransfer(
            @PathVariable Long concertId,
            @PathVariable Long orderId,
            @RequestHeader(
                    name = ORDER_ACCESS_TOKEN_HEADER,
                    required = false
            ) String accessToken
    ) throws OrderCannotBePaidException {
        return bankTransferService.startBankTransfer(
                concertId,
                orderId,
                accessToken
        );
    }

    @GetMapping("/api/orders/{publicId}/bank-transfer")
    public BankTransferInstructions getBankTransferInstructions(
            @PathVariable String publicId,
            @RequestHeader("X-Order-Access-Token") String accessToken
    ) {
        Order order = orderService.getByPublicIdAndAccessToken(
                publicId,
                accessToken
        );

        if (order.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order is not a bank transfer order"
            );
        }

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order is not awaiting payment"
            );
        }

        return bankTransferService.createInstructions(order);
    }

    @PostMapping("/api/admin/bank-statements/csv/preview")
    public void csvPreview(){ }
}