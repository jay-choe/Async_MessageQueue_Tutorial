package async.example.listen;

import async.example.listen.binder.OrderProcessBinder;
import async.example.publish.binder.DeadLetterBinder;
import async.example.publish.binder.PayResultBinder;
import async.example.service.PayService;
import message.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding({OrderProcessBinder.class})
public class OrderProcessListener {
    private static final Logger log = LoggerFactory.getLogger(OrderProcessListener.class);
    private final PayService payService;
    private final PayResultBinder payResultBinder;
    private final DeadLetterBinder deadLetterBinder;

    public OrderProcessListener(PayService payService, PayResultBinder payResultBinder, DeadLetterBinder deadLetterBinder) {
        this.payService = payService;
        this.payResultBinder = payResultBinder;
        this.deadLetterBinder = deadLetterBinder;
    }

    @StreamListener("subscribeChannel")
    public void handleOrder(final OrderMessage orderMessage) {
        log.info("==============================================================");
        log.info("{} 원 결제 요청 ===== 상품 ID: {}", orderMessage.getTotalPrice(), orderMessage.getProductId());
        log.info("==============================================================");
        boolean result = this.payService.pay(orderMessage.getTotalPrice());
        boolean sendResult;
        if (result == Boolean.FALSE) {
            log.info("DEAD LETTER QUEUE 전송");
            this.deadLetterBinder.channel().send(MessageBuilder.withPayload(orderMessage).build());
            sendResult = this.payResultBinder.channel().send(MessageBuilder.withPayload(orderMessage).build());
            if (!sendResult) {
                log.error("내역 전송 실패");
                throw new RuntimeException("Dead Queue 전송 실패");
            }
        } else {
            log.info("==============================================================");
            sendResult = this.payResultBinder.channel().send(MessageBuilder.withPayload(orderMessage).build());
            if (!sendResult) {
                log.error("내역 전송 실패");
                throw new RuntimeException("결과 전송 실패");
            }

            log.info("결제 완료 내역 전송완료");
        }

    }
}