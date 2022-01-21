package KYHjpabook.KYHjpashop.api;

import KYHjpabook.KYHjpashop.domain.Address;
import KYHjpabook.KYHjpashop.domain.Order;
import KYHjpabook.KYHjpashop.domain.OrderStatus;
import KYHjpabook.KYHjpashop.repository.OrderRepository;
import KYHjpabook.KYHjpashop.repository.OrderSearch;
import KYHjpabook.KYHjpashop.repository.order.simplequery.OrderSimpleQueryDto;
import KYHjpabook.KYHjpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        // 무한루프에 빠짐 Order <-> Member (`Member, OrderItem, Delivery` 에서 Order 컬럼에 @JsonIgnore 해줘야하지만 그래도 500 에러남)
        // Member객체는 lazyLoading으로 설정해놓았기 때문에, proxy객체를 들고 있게 되고, proxy 관련 에러가 난다.
        // Jackson 라이브러리가 루프를 돌리는 과정에서 Order를 가지고 Member를 뽑아보려고 하는 시점에, Member객체가 아닌 프록시Member 객체를 마주치게 되고 에러를 뿜어냄 -> hibernate5Module 을 추가하고, force_lazy_loading 을 세팅함으로서 해결가능
        // 하지만, force_lazy_loading을 함으로서 원하는 결과만 가져오는게 아니고 연관된 모든 데이터를 불러오게 된다 -> 성능상의 문제가 생김 ( 결론은 쓰면 안됨 )
        // 그리고, 애초에 엔티티를 직접 건드리는 방식은, 엔티티가 바뀌어버리면, api 스펙 자체가 바뀌어버리는 문제가 있다.
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        // FORCE_LAZY_LOADING 을 사용하지 않고, 필요한 데이터만 Lazy 강제 초기화하는 방법
        for (Order order : all) {
            order.getMember().getName();  // Lazy 강제 초기화  (`order.getMember()` 시점까지는 프록시 객체이지만 .getName()을 하는 순간 진짜 객체화?? -> 검증 후 내용 수정 해놓기)
            order.getDelivery().getAddress();  // Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("api/v2/simple-orders")
    // 이상적인 것은 `Result<T>` 로 감싸서 반환해야 하지만, 예제의 간결함을 위해 그냥 List로 반환
    public List<SimpleOrderDto> ordersV2() {
        // Order 2개
        // N + 1 -> 1 + 회원N + 배송N : Order 2개 기준일때 최대 5번의 쿼리가 실행 (최대 5번이라고 한 것은 쿼리를 날리기 전에 영속성 컨텍스트를 뒤지기 때문이다. 영속성 컨텍스트에 있다면 쿼리를 날리지 않는다.)
        // N+1 문제 발생. Order에서 Delivery와 Member를 EAGER로 바꾸어도 문제 해결 안됨(EAGER로 하면 예측하기도 어려움 -> 실무에서 쓰면 안됨)
        // EAGER로 바꾸어도 처음 Order를 가지고 오고 EAGER를 발견 -> EAGER로 되어있네! 하면서 이것저것 연관된것을 다 긁어오기 시작함 -> 망함... Lazy로 두어야한다. 필요할때만 fetch조인을 선택적으로 활용하는 식으로 문제를 해결해야한다.
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
        
        // `inline variable 단축키`를 활용하여 3줄로 줄이기
//        return orderRepository.findAllByString(new OrderSearch()).stream()
//                .map(o -> new SimpleOrderDto(o))
//                .collect(Collectors.toList());
    }

    @GetMapping("api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();  // LAZY 초기화 ( 식별자를 가지고 영속성 컨텍스트에서 찾음 -> 없으면 DB 쿼리 날림)
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();  // LAZY 초기화 ( 식별자를 가지고 영속성 컨텍스트에서 찾음 -> 없으면 DB 쿼리 날림)
        }
    }

}