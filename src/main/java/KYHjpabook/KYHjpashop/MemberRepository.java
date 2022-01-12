package KYHjpabook.KYHjpashop;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public Long save(Member member) {
        em.persist(member);
        return member.getId(); // member를 넘기지 않고 id만 리턴해줌 (사이드이펙트를 최소화 하려는 취향.)
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }

}