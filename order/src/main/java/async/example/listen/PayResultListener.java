package async.example.listen;

import async.example.listen.binder.PayResultBinder;
import async.example.service.OrderMqService;
import lombok.RequiredArgsConstructor;
import message.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
@EnableBinding({PayResultBinder.class})
@RequiredArgsConstructor
public class PayResultListener {
    private static final Logger log = LoggerFactory.getLogger(PayResultListener.class);
    private final OrderMqService orderMqService;

    @StreamListener("resultChannel")
    public void handleOrderResult(final OrderMessage message) {
        log.info("==============================================================");
        log.info("{} 원 결제 요청 완료 ===== 상품 ID: {}", message.getTotalPrice(), message.getProductId());
        log.info("==============================================================");
        this.orderMqService.handlePaymentResult(message);
    }
}
