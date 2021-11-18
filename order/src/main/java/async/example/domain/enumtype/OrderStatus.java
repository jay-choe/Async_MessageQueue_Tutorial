package async.example.domain.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    WAITING_FOR_PAYMENT("waiting_for_payment"),
    ASYNC_ORDER_REQUEST_COMPLETE("async_order_request_complete"),
    COMPLETE("complete"),
    FAILED("failed");

    private final String orderStatus;
}
