package KYHjpabook.KYHjpashop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

// Junit4의 Runwith과 같은 기능을 하는 Junit5 어노테이션 [출처: https://hirlawldo.tistory.com/39 [도비의 블로그]]
// 하지만 dlal `@SpringBootTest` 에는 `@ExtendWith(SpringExtension.class)`가 포함되어 있다. 그래서 생략 가능  [https://www.whiteship.me/springboot-no-more-runwith/]
//@ExtendWith(SpringExtension.class)
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    // `@Transactional`이 `@Test` 와 같이 쓰이면 test 완료 후 데이터를 롤백한다. 로그에서도 `Rolled back transaction for test`를 확인할 수 있다.
    // `org.springframework` 것을 사용하기를 권장. because 할 수 있는 옵션이 많음
    @Transactional
    @Rollback(false)
    public void testMember() throws Exception {
        // given
        Member member = new Member();
        member.setUsername("memberA");

        // when
        Long saveId = memberRepository.save(member);
        Member findMember = memberRepository.find(saveId);

        // then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);  // findMember == member
        // 같은 transaction 안에서 저장을하고 조회를 하면, 영속성 컨텍스트가 같다. 같은 영속성 컨텍스트 안에서는 id값이 같으면 같은 객체임을 보장한다. 영속성 컨텍스트에서 객체를 반환한 것(따라서 select문도 나가지 않음)
        System.out.println("findMember == member = " + (findMember == member));
    }
}