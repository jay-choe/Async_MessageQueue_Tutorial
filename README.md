# 비동기 처리를 위한 MessageQueue사용

### 실습환경 설정
```
## 레퍼지토리 받기 
git clone -b tester https://github.com/jaychoe/Async_MessageQueue_Tutorial.git 

## docker 실행하기 - 일반환경 
docker-compose -f docker-infra-compose.yml up -d 

## docker 실행하기 - M1 
docker-compose -f docker-infra-compose-m1.yml up -d
```

### 쿠팡의 결제 순서
<img width="841" alt="스크린샷 2021-11-24 오후 1 13 55" src="https://user-images.githubusercontent.com/68694844/143174123-ef984fd5-45e3-4327-a939-1ac888e1f235.png">


### 소개

이 프로젝트는 42Seoul 교육생들에게 백엔드 동기 요청과 비동기 요청의 문제점을 코드로 직접 느끼게 하고 이러한 문제들을 MessageQueue를 이용하여 처리하는 방식을 적용시켜보게 하는 교육 프로젝트입니다.

### 문제

42쿠팡은 OrderService와 PaymentService를 이용하고 있습니다.
그러나 PaymentService가 20%의 확률로 결제에 실패합니다.
이러한 상황을 어떻게 해결해 나가야할까요?

### 순서

1. 동기 요청을 만들어보고 그에 대한 문제점을 확인해보자.
2. 비동기 요청을 만들어보고 그에 대한 문제점을 확인해보자.
3. MessageQueue를 어떻게 사용하면 좋을지 서비스 아키텍처를 그림으로 설명해보자.
4. RabbitMQ와 Kafka를 Spring Cloud Stream을 이용하여 작동시켜보자.


### 서비스 아키텍처
<img width="691" alt="스크린샷 2021-11-24 오후 1 14 17" src="https://user-images.githubusercontent.com/68694844/143174180-f16f18fd-4ce1-4ebb-b7a6-651b7620a2d2.png">
