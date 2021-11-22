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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OrderSyncService {
    RestTemplate restTemplate = new RestTemplate();
    private static final String paymentUrl = "http://localhost:20002/payment";
    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;

    public OrderSyncService(
        OrderLogRepository orderLogRepository,
        ProductRepository productRepository, OrderBinder orderBinder) {
        this.orderLogRepository = orderLogRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public boolean orderSync(OrderRequest orderRequest) {
        Product product = productRepository.findById(orderRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));

        stockCheck(product.getStock(), orderRequest.getStock());

        log.info("============주문 생성중==========");
        OrderLog orderLog = OrderLog.builder()
            .productId(product.getId())
            .productName(product.getName())
            .productPrice(product.getPrice())
            .orderStock(orderRequest.getStock())
            .status(OrderStatus.WAITING_FOR_PAYMENT)
            .build();
        orderLogRepository.save(orderLog);
        log.info("==========주문 내역 생성=============");
        Long totalPrice = product.getPrice() * orderRequest.getStock();
        ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
        log.info("==========결제 요청 =============");
        log.info("결제 요청 결과: {}", response.getBody());
        if (response.getBody().equals("성공")) {
            log.info("결제에 성공했습니다.");
            product.updateStock(orderRequest.getStock());
            productRepository.save(product);
            orderLog.setStatus(OrderStatus.COMPLETE);
            orderLogRepository.save(orderLog);
            return true;
        } else {
            log.info("결제에 실패했습니다.");
            orderLog.setStatus(OrderStatus.FAILED);
        }
        return false;
    }

    private void stockCheck(Integer prodStock, Integer reqStock) {
        if (prodStock < reqStock) {
            log.error("재고가 부족합니다.");
            throw new RuntimeException("재고가 부족합니다.");
        }
    }
}
