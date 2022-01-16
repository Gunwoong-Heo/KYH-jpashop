package KYHjpabook.KYHjpashop.service;

import KYHjpabook.KYHjpashop.domain.Member;
import KYHjpabook.KYHjpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 테스트코드에 붙었을때만 데이터를 롤백시킨다.(일반적인 경우에는 아님)
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;  // 필요는 없지만 쿼리 확인용으로 추가.

    @Test
//    @Rollback(false)
    public void 회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        Long savedId = memberService.join(member);

        // then
        em.flush();  // @Rollback(false)를 하지 않고, DB에 insert쿼리를 날리는 것을 log로 볼 수 있다. 들어간 값은 `@Transactional`에 의해 rollback된다.
        assertEquals(member, memberRepository.findOne(savedId));  // jpa에서는 같은 트랜잭션 안에서 pk값이 같으면 같은 영속성 컨텍스트에서 관리되는 같은 엔티티로 볼 수 있다.
    }

    @Test
    public void 중복_회원_예외() throws Exception {
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);
/*
        // assertThrows 로 대체
        try {
            memberService.join(member2);
        } catch (IllegalStateException e) {
            return;
        }
*/
        assertThrows(IllegalStateException.class, () -> memberService.join(member2));

        // then
//        fail("예외가 발생해야 한다.");
    }

}