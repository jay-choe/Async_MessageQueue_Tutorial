package async.example.listen.binder;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface PayResultBinder {
    String MESSAGE_CHANNEL_NAME = "resultChannel";

    @Input("resultChannel")
    SubscribableChannel channel();
}
