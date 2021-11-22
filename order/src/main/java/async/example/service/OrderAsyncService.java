package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.enumtype.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import message.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderAsyncService {
    RestTemplate restTemplate = new RestTemplate();
    private static final String paymentUrl = "http://localhost:20002/payment";

    private final CommonService commonService;

    @Transactional
    public void orderAsync(OrderRequest orderRequest) {
        // before payment
        Product product = commonService.findProduct(orderRequest.getProductId());
        int requestStock = orderRequest.getStock();
        Long totalPrice = product.getPrice() * requestStock;
        OrderLog orderLog = commonService.checkStockAndCreateOrder(product, requestStock, OrderStatus.WAITING_FOR_PAYMENT);

        new Thread( () ->
        {
            ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
            if (response.getBody().equals("실패")) {
                log.error("=================결제 실패===============");
                log.info("실패 내역- 요청ID: {}, 상품ID:{} , 금액: {}", orderLog.getId(), orderLog.getProductId(), totalPrice);
                orderLog.setStatus(OrderStatus.FAILED); // 이거 왜 DB에 반영되지 않을까?  ? ?
            } else {
                orderAsyncResult(orderLog.getId());
            }
        }).start();
        log.info("결제 요청 완료");
    }

    // 비동기 요청 완료 후
    @Transactional
    public void orderAsyncResult(Integer orderId) {
        OrderLog orderLog = commonService.findOrderLog(orderId);
        log.info("주문 요청 처리 완료");
        Product product = commonService.findProduct(orderLog.getProductId());
        log.info("재고 차감 중");
        log.info("========================");
        commonService.updateStockAndSaveOrder(product, orderLog.getOrderStock(), orderLog);
        log.info("주문 상태 변경 및 재고 차감 완료");
    }

}
