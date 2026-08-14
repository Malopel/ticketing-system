package accordion_symphonic.ticketing.config;

import accordion_symphonic.ticketing.config.dto.ShopConfigResponse;
import accordion_symphonic.ticketing.order.OrderProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop-config")
public class ShopConfigController {

    private final OrderProperties orderProperties;

    public ShopConfigController(
            OrderProperties orderProperties
    ) {
        this.orderProperties = orderProperties;
    }

    @GetMapping
    public ShopConfigResponse getShopConfig() {
        return new ShopConfigResponse(
                orderProperties.maxTicketsPerOrder()
        );
    }
}
