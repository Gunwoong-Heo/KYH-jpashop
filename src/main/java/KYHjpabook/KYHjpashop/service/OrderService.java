package KYHjpabook.KYHjpashop.service;

import KYHjpabook.KYHjpashop.domain.Delivery;
import KYHjpabook.KYHjpashop.domain.Member;
import KYHjpabook.KYHjpashop.domain.Order;
import KYHjpabook.KYHjpashop.domain.OrderItem;
import KYHjpabook.KYHjpashop.domain.item.Item;
import KYHjpabook.KYHjpashop.repository.ItemRepository;
import KYHjpabook.KYHjpashop.repository.MemberRepository;
import KYHjpabook.KYHjpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문 
     */
    @Transactional
    public Long order (Long MemberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepository.findOne(MemberId);
        Item item = itemRepository.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 상품정보 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
//        OrderItem orderItem1 = new OrderItem();  // 기본생성자를 protected로 해놓았기 때문에 오류가남.

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order);
        // `orderItem`과 `delivery`는 save하지 않고 `order`만 save하는 이유는 `Order엔티티`의 `orderItem`와 `delivery`에 CASCADE 옵션이 걸려 있기 때문
        // `orderItem`과 `delivery`는 다른 엔티티에서 복잡하게 참조하고 있는게 아니라 `Order엔티티`에서만 단독으로 참조하고 있기 때문에 CASECADE 옵션을 사용한 것이다.(다른 엔티티들에서 참조를 하고 있다면 CASCADE를 사용하면 안된다. 예시) Order를 변경했는데, 다른 곳에서 참조하고 있는 delivery도 변경되어버리면 되면 문제가 생김)

        // orderId 반환
        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();  // dirty checking 발생 (변경 내역 감지)
    }

/*
    참고: 주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다.
    서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다.
    이처럼 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을
    도메인 모델 패턴(http://martinfowler.com/eaaCatalog/domainModel.html)이라 한다.
    반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을
    트랜잭션 스크립트 패턴(http://martinfowler.com/eaaCatalog/transactionScript.html)이라 한다.
*/


    /**
     * 검색
     */
}