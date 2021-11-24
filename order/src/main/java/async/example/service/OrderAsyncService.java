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
    public void order(OrderRequest orderRequest) {
        // before payment
        Product product = commonService.findProduct(orderRequest.getProductId());
        int requestStock = orderRequest.getStock();
        Long totalPrice = product.getPrice() * requestStock;
        OrderLog orderLog = commonService.checkStockAndCreateOrder(product, requestStock);

        /*
        request payment, after payment request (아래에 구현해보세요)
        결제 요청 후 아래 메소드 사용 가능합니다.
        commonService.saveSuccessOrderAndUpdateStock2
        commonService.saveFailOrder2
        */
    }
}
