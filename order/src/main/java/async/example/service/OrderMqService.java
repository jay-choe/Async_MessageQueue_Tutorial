package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.entity.repository.OrderLogRepository;
import async.example.domain.entity.repository.ProductRepository;
import async.example.domain.enumtype.OrderStatus;
import async.example.publish.binder.OrderBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import message.OrderMessage;
import message.OrderRequest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderMqService {

    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;
    private final OrderBinder orderBinder;

    public void orderAsyncMessaging(OrderRequest orderRequest) {
        Product product = productRepository.findById(orderRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        OrderLog orderLog = OrderLog.builder()
            .productId(product.getId())
            .productName(product.getName())
            .productPrice(product.getPrice())
            .orderStock(orderRequest.getStock())
            .status(OrderStatus.WAITING_FOR_PAYMENT)
            .build();
        orderLogRepository.save(orderLog);
        Long totalPrice = product.getPrice() * orderRequest.getStock();
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
        orderLogRepository.save(orderLog);
    }

    // 메세지 큐로 받은 결제의 결과를 처리하는 함수
    @Transactional
    public void handlePaymentResult(OrderMessage message) {
        OrderLog orderLog = orderLogRepository.findById(message.getLogId()).orElseThrow(() -> new RuntimeException("주문내역이 존재하지 않습니다."));
        orderLog.setStatus(OrderStatus.COMPLETE);
        Product product = productRepository.findById(message.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        product.updateStock(message.getStock());
        log.info("======================");
        log.info("주문 결과 처리 완료");
    }


    private void stockCheck(Integer prodStock, Integer reqStock) {
        if (prodStock < reqStock) {
            log.error("재고가 부족합니다.");
            throw new RuntimeException("재고가 부족합니다.");
        }
    }
}
