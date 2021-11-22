package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.entity.repository.OrderLogRepository;
import async.example.domain.entity.repository.ProductRepository;
import async.example.domain.enumtype.OrderStatus;
import async.example.publish.binder.OrderBinder;
import lombok.extern.slf4j.Slf4j;
import message.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OrderAsyncService {
    RestTemplate restTemplate = new RestTemplate();
    private static final String paymentUrl = "http://localhost:20002/payment";
    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;

    public OrderAsyncService(
        OrderLogRepository orderLogRepository,
        ProductRepository productRepository, OrderBinder orderBinder) {
        this.orderLogRepository = orderLogRepository;
        this.productRepository = productRepository;
    }

    public void orderAsync(OrderRequest orderRequest) {
        Product product = productRepository.findById(orderRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));

        stockCheck(product.getStock(), orderRequest.getStock());

        log.info("주문 생성중");
        OrderLog orderLog = OrderLog.builder()
            .productId(product.getId())
            .productName(product.getName())
            .productPrice(product.getPrice())
            .orderStock(orderRequest.getStock())
            .status(OrderStatus.ASYNC_ORDER_REQUEST_COMPLETE)
            .build();
        orderLogRepository.save(orderLog);
        log.info("==========주문 내역 생성=============");
        Long totalPrice = product.getPrice() * orderRequest.getStock();
        new Thread( () ->
        {
            ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
            if (response.getBody().equals("실패")) {
                log.error("=================결제 실패===============");
                log.info("실패 내역- 요청ID: {}, 상품ID:{} , 금액: {}", orderLog.getId(), orderLog.getProductId(), totalPrice);
            } else {
                log.error("=================결제 성공===============");
                orderAsyncResult(orderLog.getId());
            }
        }).start();
        log.info("결제 요청 완료");
    }

    // 비동기 요청 완료 후
    public void orderAsyncResult(Integer orderId) {
        OrderLog orderLog = orderLogRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 주문내역입니다."));
        log.info("주문 요청 처리 완료");
        Product product = productRepository.findById(orderLog.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        log.info("재고 차감 중");
        log.info("========================");
        orderLog.setStatus(OrderStatus.COMPLETE);
        product.updateStock(orderLog.getOrderStock());
        productRepository.save(product);
        orderLogRepository.save(orderLog);
        log.info("주문 상태 변경 및 재고 차감 완료");
    }

    private void stockCheck(Integer prodStock, Integer reqStock) {
        if (prodStock < reqStock) {
            log.error("재고가 부족합니다.");
            throw new RuntimeException("재고가 부족합니다.");
        }
    }
}
