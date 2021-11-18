package async.example.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AsyncRequest {
    private Integer orderId;
    private Long totalPrice;
}
