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
    }
}
