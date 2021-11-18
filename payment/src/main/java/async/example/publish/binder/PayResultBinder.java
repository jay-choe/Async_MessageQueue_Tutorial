package async.example.publish.binder;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface PayResultBinder {
    String MESSAGE_CHANNEL_NAME = "resultChannel";

    @Output("resultChannel")
    MessageChannel channel();
}
