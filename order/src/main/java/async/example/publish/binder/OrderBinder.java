package async.example.publish.binder;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface OrderBinder {
    String MESSAGE_CHANNEL_NAME = "publishChannel";

    @Output("publishChannel")
    MessageChannel channel();
}