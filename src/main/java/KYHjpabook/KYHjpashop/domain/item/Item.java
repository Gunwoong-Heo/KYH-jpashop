package KYHjpabook.KYHjpashop.domain.item;

import KYHjpabook.KYHjpashop.domain.Category;
import KYHjpabook.KYHjpashop.domain.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter // @Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // 데이터를 가지고 있는 쪽에 비즈니스 메소드가 있는게 응집력이 좋다.
    // 데이터를 변경할 일이 있으면 setter로 변경하는게 아니라, 핵심 비즈니스 메소드를 통해서 변경해야한다.
    // ==비즈니스 로직== //
    /**
     * stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

}