package async.example.controller;

import async.example.service.OrderAsyncService;
import async.example.service.OrderMqService;
import async.example.service.OrderService;
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

    private final OrderService orderService;
    private final OrderSyncService orderSyncService;
    private final OrderAsyncService orderAsyncService;
    private final OrderMqService orderMqService;

    @PostMapping("/sync/v1")
    public ResponseEntity<String> orderV1(@RequestBody OrderRequest orderRequest) { // 단순한 동기 요청입니다.
        boolean result = orderSyncService.orderSync(orderRequest);
        if (result == Boolean.FALSE) {
            return new ResponseEntity<>("주문 실패", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>("주문 완료", HttpStatus.OK);
    }

    @PostMapping("/async/v1")
    public ResponseEntity<String> orderAsync(@RequestBody OrderRequest orderRequest) { //비동기 요청입니다.
        orderAsyncService.orderAsync(orderRequest);
        return new ResponseEntity<>("주문 요청이 생성되었습니다.", HttpStatus.OK);
    }

    @PostMapping("/async/mq")
    public ResponseEntity<String> orderAsyncByMessageQueue(@RequestBody OrderRequest orderRequest) { // 메세지 큐를 통한 비동기 요청입니다.
        orderMqService.orderAsyncMessaging(orderRequest);
        return new ResponseEntity<>("주문 요청이 생성되었습니다.", HttpStatus.OK);
    }
}
