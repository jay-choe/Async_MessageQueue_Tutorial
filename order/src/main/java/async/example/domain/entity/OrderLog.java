package async.example.domain.entity;

import async.example.domain.enumtype.OrderStatus;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class OrderLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer productId;
    private String productName;
    private Long productPrice;
    private Integer orderStock;

    @Enumerated(EnumType.STRING) @Setter
    private OrderStatus status = OrderStatus.WAITING_FOR_PAYMENT;

    @Builder
    private OrderLog(final int productId, final String productName, final long productPrice, final int orderStock, final OrderStatus status) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.orderStock = orderStock;
        this.status = status;
    }

    public static OrderLog create(Product product, int requestStock) {
        return OrderLog.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .orderStock(requestStock)
                .status(OrderStatus.WAITING_FOR_PAYMENT)
                .build();
    }

}
