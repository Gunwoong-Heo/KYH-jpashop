package KYHjpabook.KYHjpashop.Controller;

import KYHjpabook.KYHjpashop.domain.item.Book;
import KYHjpabook.KYHjpashop.domain.item.Item;
import KYHjpabook.KYHjpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("items/new")
    public String crateForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("items/new")
    public String create(BookForm form) {
        
        Book book = new Book();
        //static 생성자 메서드를 통해서 객체를 세팅하고 setter는 날려버리는게 나은 설계 -> 추후 리팩토링 할 것
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);

        return "redirect:/";
    }

    @GetMapping("items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();

        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);
        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);

        return "items/updateItemForm";

    }

/*
    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form ) {

        // 컨트롤러에서 어설프게 엔티티를 생성하지 마세요.
        // 트랜잭션이 있는 서비스 계층에 식별자( id )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto)
        // 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요.
        // 트랜잭션 커밋 시점에 변경 감지가 실행됩니다.

        Book book = new Book();
        book.setId(form.getId());
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);  // merge

        return "redirect:/items";

    }
*/
    @PostMapping(value = "/items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") BookForm form) {
        itemService.updateItem(form.getId(), form.getName(), form.getPrice());
        return "redirect:/items";
    }


}