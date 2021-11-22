package async.example.service;

import async.example.domain.entity.OrderLog;
import async.example.domain.entity.Product;
import async.example.domain.entity.repository.OrderLogRepository;
import async.example.domain.entity.repository.ProductRepository;
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
public class OrderSyncService {
    RestTemplate restTemplate = new RestTemplate();
    private static final String paymentUrl = "http://localhost:20002/payment";

    private final CommonService commonService;

    @Transactional
    public boolean orderSync(OrderRequest orderRequest) {
        // before payment
        Product product = commonService.findProduct(orderRequest.getProductId());
        int requestStock = orderRequest.getStock();
        Long totalPrice = product.getPrice() * requestStock;
        OrderLog orderLog = commonService.checkStockAndCreateOrder(product, requestStock, OrderStatus.WAITING_FOR_PAYMENT);

        // request payment
        ResponseEntity<String> response = restTemplate.postForEntity(paymentUrl, totalPrice, String.class);
        log.info("==========결제 요청 =============");
        if (response.getBody().equals("성공")) {
            commonService.updateStockAndSaveOrder(product, requestStock, orderLog);
            return true;
        }
        commonService.saveFailOrder(orderLog);
        return false;
    }
}
