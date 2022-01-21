package KYHjpabook.KYHjpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    // 원하는 것만 select 해서 가져옴 -> 재사용성 낮지만, 부하가 덜 걸림, qlString이 좀 지저분함
    // 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
    // OrderRepository는 Order 엔티티를 조회하기 위해서 사용해야 하는데 하단 코드를 OrderRepository에 넣으면 OrderRepository에 API스펙이 들어와 버리는 꼴이 된다 -> 별도의 package와 Clss를 만들어서 이동시키는게 좀 더 클린함.
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new KYHjpabook.KYHjpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }
}