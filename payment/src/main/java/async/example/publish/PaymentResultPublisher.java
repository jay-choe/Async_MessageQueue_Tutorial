package async.example.publish;

import async.example.publish.binder.PayResultBinder;
import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({PayResultBinder.class})
public class PaymentResultPublisher {
    public PaymentResultPublisher() {
    }
}