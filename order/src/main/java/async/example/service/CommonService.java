package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.entity.repository.OrderLogRepository;
import async.example.domain.entity.repository.ProductRepository;
import async.example.domain.enumtype.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonService {

    private final ProductRepository productRepository;
    private final OrderLogRepository orderLogRepository;

    public Product findProduct(int productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
    }

    public OrderLog findOrderLog(int orderId) {
        return orderLogRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 주문내역입니다."));
    }

    OrderLog checkStockAndCreateOrder(Product product, int requestStock) {
        if (product.getStock() < requestStock) {
            log.error("재고가 부족합니다.");
            throw new RuntimeException("재고가 부족합니다.");
        }
        log.info("========== 주문 내역 생성 ==========");
        OrderLog orderLog = OrderLog.create(product, requestStock);
        return orderLogRepository.save(orderLog);
    }

    void saveSuccessOrderAndUpdateStock(Product product, int requestStock, OrderLog orderLog) {
        log.info("주문요청 성공 처리 완료");
        log.info("재고 차감 완료");
        orderLog.setStatus(OrderStatus.COMPLETE);
        product.updateStock(requestStock);
    }

    void saveFailOrder(OrderLog orderLog, Long totalPrice) {
        log.info("주문요청 실패 처리 완료: 주문ID: {}, 상품ID: {}, 금액: {}",
                orderLog.getId(), orderLog.getProductId(), totalPrice);
        orderLog.setStatus(OrderStatus.FAILED);
    }

    @Transactional
    void saveSuccessOrderAndUpdateStock2(Product product, int requestStock, OrderLog orderLog) {
        log.info("주문ID: {}, 주문요청 성공 처리 완료", orderLog.getId());
        log.info("재고 차감 완료 - 상품ID: {}, 수량: {}", product.getId(), requestStock);
        orderLog.setStatus(OrderStatus.COMPLETE);
        product.updateStock(requestStock);
        productRepository.save(product);
        orderLogRepository.save(orderLog);
    }

    void saveFailOrder2(OrderLog orderLog) {
        log.info("주문ID: {} , 주문요청 실패 처리 완료", orderLog.getId());
        orderLog.setStatus(OrderStatus.FAILED);
        orderLogRepository.save(orderLog);
    }

}
