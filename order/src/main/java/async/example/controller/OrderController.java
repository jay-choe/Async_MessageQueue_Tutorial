package async.example.controller;

import async.example.service.OrderAsyncService;
import async.example.service.OrderMqService;
import async.example.service.OrderSyncService;
import lombok.RequiredArgsConstructor;
import message.OrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderSyncService orderSyncService;
    private final OrderAsyncService orderAsyncService;
    private final OrderMqService orderMqService;

    // 동기요청
    @PostMapping("/sync/v1")
    public ResponseEntity<String> orderSync(@RequestBody OrderRequest orderRequest) {
        boolean result = orderSyncService.order(orderRequest);
        if (result == Boolean.TRUE) {
            return new ResponseEntity<>("주문이 성공했습니다", HttpStatus.OK);
        }
        return new ResponseEntity<>("주문이 실패했습니다", HttpStatus.CONFLICT);
    }

    // 비동기요청
    @PostMapping("/async/v1")
    public ResponseEntity<String> orderAsync(@RequestBody OrderRequest orderRequest) {
        orderAsyncService.order(orderRequest);
        return new ResponseEntity<>("주문 요청이 완료되었으나 결제대기중입니다.", HttpStatus.OK);
    }

    // 메시지큐를 이용한 비동기요청
    @PostMapping("/async/mq")
    public ResponseEntity<String> orderAsyncByMessageQueue(@RequestBody OrderRequest orderRequest) {
        orderMqService.order(orderRequest);
        return new ResponseEntity<>("주문 요청이 완료되었으나 결제대기중입니다.", HttpStatus.OK);
    }
}
