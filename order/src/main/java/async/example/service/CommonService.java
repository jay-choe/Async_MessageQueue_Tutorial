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

    OrderLog checkStockAndCreateOrder(Product product, int requestStock, OrderStatus status) {
        if (product.getStock() < requestStock) {
            log.error("재고가 부족합니다.");
            throw new RuntimeException("재고가 부족합니다.");
        }
        log.info("============주문 내역 생성==========");
        OrderLog orderLog = OrderLog.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .orderStock(requestStock)
                .status(status)
                .build();
        return orderLogRepository.save(orderLog);
    }

    void updateStockAndSaveOrder(Product product, int requestStock, OrderLog orderLog) {
        log.info("결제에 성공했습니다.");
        orderLog.setStatus(OrderStatus.COMPLETE);
        product.updateStock(requestStock);
    }

    @Transactional
    void updateStockAndSaveOrder2(Product product, int requestStock, OrderLog orderLog) {
        log.info("결제에 성공했습니다.");
        orderLog.setStatus(OrderStatus.COMPLETE);
        product.updateStock(requestStock);
        productRepository.save(product);
        orderLogRepository.save(orderLog);
    }

    void saveFailOrder(OrderLog orderLog) {
        log.info("결제에 실패했습니다.");
        orderLog.setStatus(OrderStatus.FAILED);
    }

}
