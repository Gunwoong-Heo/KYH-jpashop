package KYHjpabook.KYHjpashop.repository;

import KYHjpabook.KYHjpashop.domain.Member;
import KYHjpabook.KYHjpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
/*
        return em.createQuery("select o from Order o join o.member m" +
                        " where o.status = :status" +
                        " and m.name like :name",
                        Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
//                .setFirstResult(100)
                .setMaxResults(1000)
                .getResultList();
*/

        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대1000건
        return query.getResultList();
    }

    // Order 자체를 select 해옴 -> 재사용성 높지만, 부하가 좀 더 걸림(생각보다 미미)
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }


    public List<Order> findAllWithItem() {
        // RDB에서 join을 하게 되면 Order가 뻥튀기가 된다. ($sql = "select * from orders o join order_item oi on o.order_id = oi.order_id")
        // Json 형식으로 반환을 활때, Order가 2건이면 object가 2건이 반환되어져야 하는데, sql 실행결과의 row수만큼 object가 반환됨. 즉, row를 그대로 Json으로 만들어서 보내버림

        // sql에서 distinct가 동작하려면 완전히 한 row의 모든 요소가 다 같아야한다.
        // jpql에서 distinct를 넣고 해당 쿼리를 실행한 결과가 로그에 찍히는데, 이 로그에 찍힌 쿼리를 가지고 db에 직접 쿼리를 날려 조회해봐도,
        // 한 row가 완전히 일치하지 않으면 distinct가 적용되지 않는다.
        // 하지만 실제로 Json으로 반환되어지는 데이터에서는 한 row가 완전히 일치하지 않음에도 불구하고 distinct가 적용됨
        // 이는 JPA에서 자체적으로 데이터를 가져와서 한 번 더 검토해주는 것이다. -> resultSet을 가지고 와서, root엔티티의 id가 똑같을 때 중복을 걸러서 컬렉션에 담아준다.
        // (resultSet을 다 가져와서 검토하는거 맞나? 이러면 일대다 에서 페이징 처리할때 메모리에서 하는 거랑 어떤것이 다른거지?)
        return em.createQuery(
                        "select distinct o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d" +
                                " join fetch o.orderItems oi " +
                                " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
                // 일대다를 fetch join 하는 순간 페이징 쿼리가 안 나감
                // 그러나 api를 호출해보면 페이징이 적용된 것처럼 Json data가 반환됨
                // 로그에 찍힌 쿼리를 보니 limit,offset 등이 보이지 않음
                // 로그에 WARN도 찍힘 (2022-01-20 16:12:00.884  WARN 44784 --- [nio-8080-exec-6] o.h.h.internal.ast.QueryTranslatorImpl   : HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!)
                // 메모리에서 페이징 처리를 해버린다는 경고! -> 데이터 1만개가 있으면, 1만개를 다 어플리케이션에 퍼올린다음에 거기서 페이징 처리를 해버림 -> 잘못하면 outOfMemory 가 나버림 -> 망함
                // 왜 이렇게 극단적인 선택을 hibernate가 할까?
                // Log에 찍힌 쿼리로 조회하면 데이터가 뻥튀기됨. 이 resultSet에서 페이징을 처리하면 문제 발생.
                // 예를 들어 우리가 의도한 Order의 갯수는 2개이고, 이 기준으로 페이징을 처리하고 싶었지만, Log로 찍힌 쿼리로 조회한 결과는 4개라면, 의도한 결과가 나올리가 없다.
                // db에서 offset,limit 을 적용하면 Order를 기준으로 적용되는게 아니라, 데이터가 뻥튀기되는 N인 OrderItem을 기준으로 페이징 처리가 되어버리는 효과가 남
                // 그래서 hibernate는 경고를 메모리에서 페이징처리를 함.
                // 데이터가 작은 상황에서는 문제가 되지 않기 때문에, 경고를 내고 메모리에서 페이징 처리해주는 것으로 보임.
                // (모든 상황에서 데이터가 커서 문제가 될만한 상황이라면, WARN가 아닌 ERROR를 내지 않았을까?? ungk피셜..)
                // 다시 결론은, 일대다 fetch join에서는 페이징 처리를 하지 말자.
                // 일대다가 아닌 거는 상관없음.(데이터 뻥튀기가 안 일어남 -> Memory 페이징 안함)
                // 참고: 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가 부정합하게 조회될 수 있다. 자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자
    }

    // 페이징도 가능하고, DB->Application으로 전송되는 데이터의 양 자체가 컴팩트하고 중복이 없다. 단점은 DB에 query를 날리는 횟수가 소폭 증가한 다는 점이지만,
    // application.yml에 `default_batch_fetch_size` 를 지정해놓으면 쿼리가 나가는 양을 줄일 수 있다. 지정하지 않고 쿼리를 날리면 N+1 문제가 발생한다.
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                        "select o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d", Order.class)
/*
                        // ToOne 관계도 빼고 조회해도 `default_batch_fetch_size`의 영향을 받아서 최적화가 됨
                        // 하지만, ToOne 관계 정도는 넣어서 조회해주면 DB에 날리는 쿼리를 줄일 수 있다.
                        "select o from Order o", Order.class)
*/
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();  // ToOne 관계들 이기 때문에 페이징 가능
    }
}