package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.entity.repository.OrderLogRepository;
import async.example.domain.entity.repository.ProductRepository;
import async.example.domain.enumtype.OrderStatus;
import async.example.publish.binder.OrderBinder;
import lombok.extern.slf4j.Slf4j;
import message.OrderMessage;
import message.OrderRequest;
import message.PayRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OrderService {
    RestTemplate restTemplate = new RestTemplate();
    private static final String paymentUrl = "http://localhost:20002/payment";
    private final OrderLogRepository orderLogRepository;
    private final ProductRepository productRepository;
    private final OrderBinder orderBinder;

    public OrderService(
        OrderLogRepository orderLogRepository,
        ProductRepository productRepository, OrderBinder orderBinder) {
        this.orderLogRepository = orderLogRepository;
        this.productRepository = productRepository;
        this.orderBinder = orderBinder;
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

    @Transactional
    public void orderUntilSucceed(OrderRequest orderRequest) {
        Product product = productRepository.findById(orderRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));

        stockCheck(product.getStock(), orderRequest.getStock());

        log.info("주문 생성중");
        OrderLog orderLog = OrderLog.builder()
            .productId(product.getId())
            .productName(product.getName())
            .productPrice(product.getPrice())
            .orderStock(orderRequest.getStock())
            .status(OrderStatus.WAITING_FOR_PAYMENT)
            .build();
        orderLogRepository.save(orderLog);
        log.info("==========주문 내역 생성=============");
        // 결제 성공시까지 계속 try
        Long totalPrice = product.getPrice() * orderRequest.getStock();
        ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
        while (response.getBody().equals("실패")) {
            response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
            log.info(response.getBody());
        }
        log.info("결제 성공");
        product.updateStock(orderRequest.getStock());
        productRepository.save(product);
        orderLog.setStatus(OrderStatus.COMPLETE);
        orderLogRepository.save(orderLog);
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
        PayRequest asyncRequest = new PayRequest(orderLog.getId(), totalPrice);
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
        orderLog.setStatus(OrderStatus.COMPLETE);
        product.updateStock(orderLog.getOrderStock());
        productRepository.save(product);
        orderLogRepository.save(orderLog);
        log.info("주문 상태 변경 및 재고 차감 완료");
    }

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
            .stock(product.getStock())
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

    public void handlePaymentResult(OrderMessage message) {
        OrderLog orderLog = orderLogRepository.findById(message.getLogId()).orElseThrow(() -> new RuntimeException("주문내역이 존재하지 않습니다."));
        orderLog.setStatus(OrderStatus.COMPLETE);
        Product product = productRepository.findById(message.getProductId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        product.updateStock(message.getStock());
        productRepository.save(product);
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
