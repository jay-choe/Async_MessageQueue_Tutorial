package async.example.service;

import async.example.domain.OrderRequestDto;
import async.example.domain.PayRequestDto;
import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.entity.repository.OrderLogRepository;
import async.example.domain.entity.repository.ProductRepository;
import async.example.domain.enumtype.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OrderService {
    RestTemplate restTemplate = new RestTemplate();
    private static final String paymentUrl = "http://localhost:20002/payment";
    private static final int SYNC = 0;
    private static final int ASYNC = 1;

    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;


    public OrderService(
        OrderLogRepository orderLogRepository,
        ProductRepository productRepository) {
        this.orderLogRepository = orderLogRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public boolean orderSync(OrderRequestDto orderRequestDto) {
        Product product = findProduct(orderRequestDto.getProductId());
        int requestStock = orderRequestDto.getStock();

        OrderLog orderLog = checkStockAndCreateOrder(product, requestStock, OrderStatus.WAITING_FOR_PAYMENT);

        Long totalPrice = product.getPrice() * requestStock;
        ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
        log.info("==========결제 요청 =============");
        log.info("결제 요청 결과: {}", response.getBody());
        if (response.getBody().equals("성공")) {
            log.info("결제에 성공했습니다.");
            saveSuccessOrder(product, requestStock, orderLog, SYNC);
            return true;
        } else {
            log.info("결제에 실패했습니다.");
            orderLog.setStatus(OrderStatus.FAILED);
        }
        return false;
    }

    @Transactional
    public void orderUntilSucceed(OrderRequestDto orderRequestDto) {
        Product product = findProduct(orderRequestDto.getProductId());
        int requestStock = orderRequestDto.getStock();

        OrderLog orderLog = checkStockAndCreateOrder(product, requestStock, OrderStatus.WAITING_FOR_PAYMENT);

        // 결제 성공시까지 계속 try
        Long totalPrice = product.getPrice() * requestStock;
        ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
        while (response.getBody().equals("실패")) {
            response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
            log.info(response.getBody());
        }
        log.info("결제 성공");
        saveSuccessOrder(product, requestStock, orderLog, SYNC);
    }

    public void orderAsync(OrderRequestDto orderRequestDto) {
        Product product = findProduct(orderRequestDto.getProductId());
        int requestStock = orderRequestDto.getStock();

        OrderLog orderLog = checkStockAndCreateOrder(product, requestStock, OrderStatus.ASYNC_ORDER_REQUEST_COMPLETE);

        Long totalPrice = product.getPrice() * requestStock;
        PayRequestDto asyncRequest = new PayRequestDto(orderLog.getId(), totalPrice);
        restTemplate.postForEntity(paymentUrl + "/async", asyncRequest, String.class);
        log.info("결제 요청 완료");
    }

    // 비동기 요청 완료 후 해당 클라이언트가 보내는 결과를 받는 메서드 입니다.
    public void orderAsyncResult(Integer orderId) {
        OrderLog orderLog = orderLogRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 주문내역입니다."));
        log.info("주문 요청 처리 완료");
        Product product = productRepository.findById(orderLog.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        log.info("재고 차감 중");
        log.info("========================");
        saveSuccessOrder(product, orderLog.getOrderStock(), orderLog, ASYNC);
        log.info("주문 상태 변경 및 재고 차감 완료");
    }

    private Product findProduct(int productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
    }

    @Transactional
    OrderLog checkStockAndCreateOrder(Product product, int requestStock, OrderStatus status) {
        int prodStock = product.getStock();
        if (prodStock < requestStock) {
            log.error("재고가 부족합니다.");
            throw new RuntimeException("재고가 부족합니다.");
        }
        log.info("============주문 내역 생성==========");
        OrderLog orderLog = OrderLog.create(product, requestStock, status);
        return orderLogRepository.save(orderLog);
    }


    @Transactional
    void saveSuccessOrder(Product product, int requestStock, OrderLog orderLog, int syncType) {
        if (syncType == SYNC) {
            product.updateStock(requestStock);
        } else {
            product.updateStock(orderLog.getOrderStock());
        }
        productRepository.save(product);
        orderLog.setStatus(OrderStatus.COMPLETE);
        orderLogRepository.save(orderLog);
    }

}
