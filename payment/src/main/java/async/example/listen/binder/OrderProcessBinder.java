package async.example.listen.binder;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface OrderProcessBinder {
    String MESSAGE_CHANNEL_NAME = "subscribeChannel";

    @Input("subscribeChannel")
    SubscribableChannel channel();
}


