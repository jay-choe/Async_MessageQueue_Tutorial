package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.enumtype.OrderStatus;
import async.example.publish.binder.OrderBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import message.OrderMessage;
import message.OrderRequest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderMqService {

    private final OrderBinder orderBinder;
    private final CommonService commonService;

    @Transactional
    public void orderAsyncMessaging(OrderRequest orderRequest) {
        // before payment
        Product product = commonService.findProduct(orderRequest.getProductId());
        int requestStock = orderRequest.getStock();
        Long totalPrice = product.getPrice() * requestStock;
        OrderLog orderLog = commonService.checkStockAndCreateOrder(product, requestStock, OrderStatus.WAITING_FOR_PAYMENT);

        OrderMessage orderMessage = OrderMessage.builder()
            .logId(orderLog.getId())
            .productId(product.getId())
            .stock(orderRequest.getStock())
            .totalPrice(totalPrice)
            .build();

        boolean sendResult = orderBinder.channel().send(MessageBuilder.withPayload(orderMessage).build());
        if (!sendResult) {
            log.error("내역 전송 실패");
            throw new RuntimeException("Dead Queue 전송 실패");
        }

        log.info("주문 요청 처리 완료");
        log.info("========================");
        orderLog.setStatus(OrderStatus.ASYNC_ORDER_REQUEST_COMPLETE);
    }

    // 메세지 큐로 받은 결제의 결과를 처리하는 함수
    @Transactional
    public void handlePaymentResult(OrderMessage message) {
        OrderLog orderLog = commonService.findOrderLog(message.getLogId());
        Product product = commonService.findProduct(orderLog.getProductId());
        commonService.updateStockAndSaveOrder(product, message.getStock(), orderLog);
        log.info("======================");
        log.info("주문 결과 처리 완료");
    }

}
