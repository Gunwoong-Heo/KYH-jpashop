package KYHjpabook.KYHjpashop.service;

import KYHjpabook.KYHjpashop.domain.Member;
import KYHjpabook.KYHjpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//@Transactional // 이미 스프링을 쓰고 있고 스프링에 의존적인 코드들이 많이 들어가 있기 때문에 `springframework.transaction.annotation.Transactional` 을 사용하는 것이 사용할 수 있는 옵션도 더 많은 등의 장점이 많다.
@Transactional(readOnly = true) // 기본적으로 `public 메소드`에 적용 되는 transactional 전략
//@AllArgsConstructor
@RequiredArgsConstructor  // final 붙은 필드만 생성자 추가
public class MemberService {

//    @Autowired  // 필드주입 -> 문제점이 많다.(바꾸기 힘들다는 점 등...)
//    private MemberRepository memberRepository;
    private final MemberRepository memberRepository;  // 생성자 인젝션 방식으로 동작할때는 변경할 일이 없기 때문에 final로 설정해주는 것을 추천. final로 설정을 하고 생성자에서 할당을 안 해줄시 컴파일 시점에서 에러를 냄.

/*

    @Autowired // setter인젝션 (런타임때, 누군가가 실수로라도 변경할 여지가 있다. -> 치명적인 단점..)
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
*/
    
/*
//    @Autowired  // 생성자 인젝션 (가장 추천할만한 방법) @Autowired를 생략해도 Spring이 인젝션 해준다.
    // Lombok을 사용하여 생성자를 만들어주면 하단에 생성자도 생략 가능하다.
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
*/

    /** 회원 가입 */
    @Transactional // 기본전략보다 우선권을 가져서 적용된다. `@Transactional`의 기본값은 `(readonly=false)` 이다.
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    // 실무에서는 여러 WAS가 동시에 실행되기 때문에, 이와 같이 중복회원을 검증하면 문제가 생길수 있다.(name을 유니크제약조건 걸기. 멀티쓰레드,동시성에 대한 이해가 추가적으로 필요하다.)
    private void validateDuplicateMember(Member member) {
        List<Member> findMember = memberRepository.findByName(member.getName());
        if(!findMember.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }
    
    /** 회원 전체 조회 */
//    @Transactional (readOnly = true)  // readOnly 옵션을 true로 두면, JPA가 성능 최적화를 해준다. ( 더티 체킹을 안하거나, db에 따라서는 읽기전용 트랜적션임을 감지하여, 리소스를 읽기 전용 모드로 세팅하는 등)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /** 특정회원 조회(id 기준으로) */
//    @Transactional (readOnly = true)
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}