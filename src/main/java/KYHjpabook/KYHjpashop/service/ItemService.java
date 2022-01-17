package KYHjpabook.KYHjpashop.service;

import KYHjpabook.KYHjpashop.domain.item.Book;
import KYHjpabook.KYHjpashop.domain.item.Item;
import KYHjpabook.KYHjpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

/*
    // 변경 감지 기능 사용 (dirty checking)
    @Transactional
    public Item updateItem(Long itemId, Book param) {
        Item findItem = itemRepository.findOne(itemId);  // findOne으로 가져온 객체는 영속성 컨텍스트에 추가됨 -> dirtyChecking의 대상
        // 실무에서는 setter를 남발하기보다는 의미 있는 메소드를 통해서 데이터를 변경해야한다.
        // 예시 : findItem.change(price, name, stockQuantity);
        findItem.setPrice(param.getPrice());
        findItem.setName(param.getName());
        findItem.setStockQuantity(param.getStockQuantity());
        // save() 메소드를 호출하여 객체를 저장하는 과정이 불필요하다.
        // `@Transactional`에 의해서 transaction이 commit이 되고, JPA에 의해 영속성 컨텍스트 flush가 일어남
        return findItem;
    }
*/
    @Transactional
    public void updateItem(Long id, String name, int price) {
        Item item = itemRepository.findOne(id);
        item.setName(name);
        item.setPrice(price);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

}