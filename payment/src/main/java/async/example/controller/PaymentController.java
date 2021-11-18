package async.example.controller;

import async.example.domain.AsyncRequest;
import async.example.service.PayService;
import lombok.RequiredArgsConstructor;
import message.PayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/payment", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PaymentController {

    private final PayService payService;

    @PostMapping
    public ResponseEntity pay(@RequestBody final Long price) {

        boolean result = payService.pay(price);
        if (result == false) {
            return new ResponseEntity("실패", HttpStatus.OK);
        } else {
            return new ResponseEntity("성공", HttpStatus.OK);
        }
    }

    @PostMapping("/async")
    public ResponseEntity payAsync(@RequestBody final PayRequest payRequest) {
        new Thread(() -> payService.payTryToSucceed(payRequest)).start();
        return new ResponseEntity("결제 요청 완료", HttpStatus.OK);
    }
}
