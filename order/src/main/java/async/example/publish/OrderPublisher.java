package async.example.publish;

import async.example.publish.binder.OrderBinder;
import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({OrderBinder.class})
public class OrderPublisher {
    public OrderPublisher() {
    }
}

