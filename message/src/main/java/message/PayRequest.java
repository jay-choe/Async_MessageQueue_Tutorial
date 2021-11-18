package message;

public class PayRequest {
    private Integer orderId;
    private Long totalPrice;

     public PayRequest() {
    }

     public PayRequest( final Integer orderId,  final Long totalPrice) {
         this.orderId = orderId;
         this.totalPrice = totalPrice;
    }

     public Integer getOrderId() {
         return this.orderId;
    }

     public Long getTotalPrice() {
         return this.totalPrice;
    }

}
