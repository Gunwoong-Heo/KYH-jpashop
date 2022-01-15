package KYHjpabook.KYHjpashop.repository;

import KYHjpabook.KYHjpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor  // final 붙은 필드만 생성자 추가
public class MemberRepository {

/*
    @PersistenceContext  // Spring이 EntityManager를 만들어서 주입해줌
    private EntityManager em;
*/

    // 원칙적으로는 EntityManager에는 `@PersistenceContext` 를 붙여줘야 하지만 SpringDataJpa가 @Autowired로도 주입 받을수 있게 도와줌
    // 롬복으로 생성자 주입 + @Autowird 생략 해도 스프링부트가 생성자로 injection 해줌
    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

}