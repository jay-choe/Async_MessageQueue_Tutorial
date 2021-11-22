package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.entity.repository.OrderLogRepository;
import async.example.domain.entity.repository.ProductRepository;
import async.example.domain.enumtype.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Order;
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

    @Transactional
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

    @Transactional
    void updateStockAndSaveOrder(Product product, int requestStock, OrderLog orderLog) {
        log.info("결제에 성공했습니다.");
        orderLog.setStatus(OrderStatus.COMPLETE);
        product.updateStock(requestStock);
    }

    void saveFailOrder(OrderLog orderLog) {
        log.info("결제에 실패했습니다.");
        orderLog.setStatus(OrderStatus.FAILED);
    }

}
