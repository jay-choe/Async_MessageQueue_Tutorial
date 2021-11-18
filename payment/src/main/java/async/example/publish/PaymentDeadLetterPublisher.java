package async.example.publish;

import async.example.publish.binder.DeadLetterBinder;
import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({DeadLetterBinder.class})
public class PaymentDeadLetterPublisher {
    public PaymentDeadLetterPublisher() {
    }
}
