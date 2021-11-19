package async.example.listen;

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
@EnableBinding({DeadLetterBinder.class})
public class DeadLetterListener {
    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);
    private final PayService payService;
    private final PayResultBinder payResultBinder;

    public DeadLetterListener(PayService payService, PayResultBinder payResultBinder) {
        this.payService = payService;
        this.payResultBinder = payResultBinder;
    }

    @StreamListener("deadLetterChannel")
    public void handleDeadLetterQueue(OrderMessage message) {
        log.info("==================================");
        log.info("Dead Letter Get");
        log.info("==================================");
        this.payService.payToSucceedInDeadLetterQueue(message);
        boolean sendResult = this.payResultBinder.channel().send(MessageBuilder.withPayload(message).build());
        if (!sendResult) {
            log.error("내역 전송 실패");
            throw new RuntimeException("결과 전송 실패");
        } else {
            log.info("결제 완료 내역 전송완료");
        }
    }
}
