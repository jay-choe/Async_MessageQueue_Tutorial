package async.example.publish.binder;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface DeadLetterBinder {
    String MESSAGE_CHANNEL_NAME = "deadLetterChannel";

    @Output("deadLetterChannel")
    MessageChannel channel();
}

