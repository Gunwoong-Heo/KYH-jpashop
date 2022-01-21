package KYHjpabook.KYHjpashop.api;

import KYHjpabook.KYHjpashop.domain.Address;
import KYHjpabook.KYHjpashop.domain.Order;
import KYHjpabook.KYHjpashop.domain.OrderItem;
import KYHjpabook.KYHjpashop.domain.OrderStatus;
import KYHjpabook.KYHjpashop.repository.OrderRepository;
import KYHjpabook.KYHjpashop.repository.OrderSearch;
import KYHjpabook.KYHjpashop.repository.order.query.OrderFlatDto;
import KYHjpabook.KYHjpashop.repository.order.query.OrderItemQueryDto;
import KYHjpabook.KYHjpashop.repository.order.query.OrderQueryDto;
import KYHjpabook.KYHjpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // 엔티티 직접 노출방식
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            // Lazy 강제 초기화
            // Hibernate5Module 기본 설정을 사용하고 있다. 기본 설정을 사용할때는, LazyLoading 했을때 proxy객체는 데이터를 뿌리지 않는다.
            // 단, 데이터를 강제 초기화 해주면, 데이터가 있기 때문에 데이터를 뿌려준다. 
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        // 확인용 (디버거 모드로 봐도 됨)
        for (Order order : orders) {
            System.out.println("order ref = " + order + " id = " + order.getId());
        }

        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        // 확인용 (디버거 모드로 봐도 됨)
        for (Order order : orders) {
            System.out.println("order ref = " + order + " id = " + order.getId());
        }

        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4 () {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5 () {
        return orderQueryRepository.findAllByDto_optimization();  // 실제로 변수명을 이렇게 짓지는 않겠지만.. 예시니까..
    }

//    @GetMapping("/api/v6/orders")
//    public List<OrderFlatDto> ordersV6 () {
//        return orderQueryRepository.findAllByDto_flat();  // 데이터 뻥튀기, API 스펙이 `ordersV5`처럼 나와야 한다면 안 맞을 수 있음.
//    }

//            장점
//    Query: 1번
//            단점
//    쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가
//    추가되므로 상황에 따라 V5 보다 더 느릴 수 도 있다.
//    애플리케이션에서 추가 작업이 크다.
//    페이징 불가능 (db에서 긁어올때 이미 뻥튀기되어진것을 긁어와서 애플리케이션 단에서 지지고 볶고 해서 데이터 셋을 구축한것임) (OrderItem 기준으로 데이터 뻥튀기됨)
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6 () {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        // `OrderQueryDto`에 `@EqualsAndHashCode(of = "orderId")` 추가해야 정상동작 (groupingBy 할때 뭘 기준으로 할지 알려줘야함)
        return flats.stream()
                .collect(Collectors.groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());
    }


//    @Data  // `@Data` 자체가 여러가지 어노테이션을 포함하고 있기 때문에, 필요하지 않은 옵션들이 있다면 @Data가 아닌 다른 세분화된 어노테이션들만 추가하는 식으로 변경해도 된다.
    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;  // ValueObject 같은 경우는 노출해도 됨.(엔티티는 안됨) (근데 Address 같은 경우도 세분화된 항목들이 추가가 될 수 있지 않나??)
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
/*
            // Dto로 감싸서 반환한다는 것은 단순히 껍데기만 Dto로 감싸서 반환한다는 의미가 아니다. 완전히 엔티티에 대한 의존을 끊어야한다.
            // OrderItem에 Item을 순회하면서 강제 초기화 시키고, Item엔티티를 OrderDto에 넣어서 반환하면 안된다.
            // OrderItem,Item 엔티티가 외부에 노출됨. ( 이 마저도 다 Dto로 바꾸어야 한다)
            // OrderItem,Item 엔티티를 의존해버리면, 해당 엔티티가 바뀌면 API 반환값 자체가 달라져버리는 문제 발생
            order.getOrderItems()
                    .stream()
                    .forEach(o -> o.getItem().getName());
            orderItems = order.getOrderItems();  // 프록시 엔티티
*/
            orderItems = order.getOrderItems()
                    .stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
