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
            em.merge(item);
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