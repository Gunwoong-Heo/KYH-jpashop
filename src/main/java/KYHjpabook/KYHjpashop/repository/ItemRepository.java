package KYHjpabook.KYHjpashop.repository;

import KYHjpabook.KYHjpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    // 원칙적으로는 EntityManager에는 `@PersistenceContext` 를 붙여줘야 하지만 SpringDataJpa가 @Autowired로도 주입 받을수 있게 도와줌
    // 롬복으로 생성자 주입 + @Autowird 생략 해도 스프링부트가 생성자로 injection 해줌
    private final EntityManager em;

    public void save(Item item) {
        if(item.getId() == null) {
            em.persist(item);
        } else {
            // 주의: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경된다. 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)
            Item merge = em.merge(item);  // `em.merge`의 파라미터로 넘겨지는 item이 영속성 컨텍스트에 관리되는 객체가 되는 것은아니고, em.merge()를 통해 반환되는 객체가 영속성 컨텍스트로 관리되는 객체이다.
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}