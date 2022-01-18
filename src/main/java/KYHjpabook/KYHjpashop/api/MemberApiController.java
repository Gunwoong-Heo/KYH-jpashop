package KYHjpabook.KYHjpashop.api;

import KYHjpabook.KYHjpashop.domain.Member;
import KYHjpabook.KYHjpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;


//    V1 엔티티를 Request Body에 직접 매핑
//            [문제점]
//    엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
//    엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
//    실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한
//    모든 요청 요구사항을 담기는 어렵다.
//    엔티티가 변경되면 API 스펙이 변한다.
//            [결론]
//    API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();  // 반환값이 array로 반환되는 것도 문제이다.-> 확장성이 떨어진다. (예를 들어, count를 추가해 달라는 요청이 오면, 해당 array 안에 넣을 수는 없다. 처음부터 반환값이 object 형식이면은 쉽게 추가 가능. {"count":"3", "data":[`반환되어지는 데이터`]} )
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;  // 필요한 항목이 있으면 이렇게 추가해서 함께 반환
        private T data;

/*
        // 눈으로 확인하기 위해 추가한 생성자
        public Result(T data) {
            this.data = data;
        }
*/
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }



    // 엔티티를 외부에서 Json 데이터를 바인딩 받는 용도로 쓰면 안된다.
    // 엔티티가 바뀌면 api 스펙이 바뀌어버린다. 이로 인해 큰 장애로 이루어질 수 있다. -> api 스펙을 위한 별도의 DTO를 만드는 것이 좋다.
    // 그냥 api를 만들떄는 항상 엔티티를 파라미터로 받지 앖는 것이 좋다. (엔티티를 외부에 노출해서도 안된다.)
    // 엔티티로 받으면 api 스펙 문서를 까보지 않으면, 해당 엔티티에서 무슨의도로 어떤 값들이 넘어오는지 모른다.
    // DTO를 만들면 해당 DTO의 스펙이 정의되고, 의도 또한 알 수 있다. -> 무슨 값을(field) 무엇을 위해(DTO의 이름으로 유추) 넘기는지 등등
     @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {  // Json 형식으로 온 데이터를 Member객체에 매핑해줌  // @Valid를 추가가여 객체를 검증할 수 있다. 여기서는 Member객체 에서 name필드에 @NotEmpty를 추가하여 필수값으로 지정.( 엔티티의 필드에 직접적으로 @NotEmpty를 넣어서 검증하는 것은 좋지 않다. 예를 들면, 다른 메소드에서 엔티티를 가지고 무엇인가를 할때에는 @NotEmpty가 필요없는 상황이 있을 수도 있다.
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty  // 요청을 받는 DTO를 만들고 거기에 validation을 함으로서 엔티티 자체에 validation을 하여, 엔티티 코드가 더러워지는 것을 막을 수 있다.
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

}