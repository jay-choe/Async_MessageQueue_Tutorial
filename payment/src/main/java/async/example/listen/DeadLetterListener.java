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
    private final DeadLetterBinder deadLetterBinder;

    public DeadLetterListener(PayService payService, PayResultBinder payResultBinder,
        DeadLetterBinder deadLetterBinder) {
        this.payService = payService;
        this.payResultBinder = payResultBinder;
        this.deadLetterBinder = deadLetterBinder;
    }

    @StreamListener("deadLetterChannel")
    public void handleDeadLetterQueue(OrderMessage message) {
        log.info("==================================");
        log.info("Dead Letter Get");
        log.info("==================================");
        boolean payResult = this.payService.pay(message.getTotalPrice());
        // 재결제 시도 실패 시
        if (payResult == false) {
            message.setErrorRetryCount(message.getErrorRetryCount() + 1);
            log.error("======재결제 시도 실패=========");
            log.error("======재결제 시도 횟수: {} ", message.getErrorRetryCount());
            if (message.getErrorRetryCount() > 3) {
                // 결제 실패가 3회 이상 -> 관리자 확인하도록 로그
                log.error("관리자 확인 요망");
                return ;
            } else {
                deadLetterBinder.channel().send(MessageBuilder.withPayload(message).build());
                return ;
            }
        }
        // 결재 성공시
        boolean sendResult = this.payResultBinder.channel().send(MessageBuilder.withPayload(message).build());
        if (!sendResult) {
            log.error("내역 전송 실패");
            throw new RuntimeException("결과 전송 실패");
        } else {
            log.info("결제 완료 내역 전송완료");
        }
    }
}
