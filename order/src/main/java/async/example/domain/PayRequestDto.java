package async.example.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PayRequestDto {
    private Integer orderId;
    private Long totalPrice;
}
