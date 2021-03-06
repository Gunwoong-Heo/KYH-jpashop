package KYHjpabook.KYHjpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

//    @NotEmpty   // DTO에 validation을 해야한다.
    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")
    @JsonIgnore  // ordersV1()
    private List<Order> orders = new ArrayList<>();
}