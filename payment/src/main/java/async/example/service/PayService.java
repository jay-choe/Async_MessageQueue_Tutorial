package async.example.service;

import async.example.domain.AsyncRequest;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PayService {

    RestTemplate restTemplate = new RestTemplate();
    private final String RESULT_SEND_URL = "http://localhost:20001/order/async/result/v1";

    public boolean pay(Long totalPrice) {
        log.info("=============결제 진행중=================");
        log.info("======================================");
        final Random rand = new Random();
        // random -> 1 ~ 10
        int randNo = rand.nextInt(10);
        ++randNo;

        try {
            // 1 ~ 10 초 지연
            Thread.sleep(randNo * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        // 결제 성공 확률 50%
        if (randNo % 2 == 1) {
            log.error("결제에 실패하였습니다.");
            return false;
        } else {
            log.info("결제가 완료되었습니다.");
            return true;
        }
    }

    // 결제 성공 할 때까지 반복하는 함수 -> 성공시 주문 서비스에 알림
    public void payTryToSucceed(AsyncRequest req) {
        while (true) {
            boolean result = pay(req.getTotalPrice());
            if (result == Boolean.TRUE) {
                log.info("결제 성공- 주문 아이디: {}", req.getOrderId());
                log.info("결제 성공 정보를 주문서비스에 알립니다.");
                restTemplate.postForEntity(RESULT_SEND_URL, req.getOrderId(), String.class);
                return ;
            }
        }
    }
}
