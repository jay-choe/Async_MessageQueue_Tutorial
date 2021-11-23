package async.example.service;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import message.OrderMessage;
import message.OrderRequest;
import message.PayRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PayService {

    public boolean pay(Long totalPrice) {
        log.info("========== 결제 진행중 ==========");

        // random -> 1 ~ 10
        int randNo = new Random().nextInt(10) + 1;
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
}
