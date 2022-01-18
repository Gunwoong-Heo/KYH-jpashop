package KYHjpabook.KYHjpashop.Controller;

import KYHjpabook.KYHjpashop.domain.Member;
import KYHjpabook.KYHjpashop.domain.Order;
import KYHjpabook.KYHjpashop.domain.item.Item;
import KYHjpabook.KYHjpashop.repository.OrderSearch;
import KYHjpabook.KYHjpashop.service.ItemService;
import KYHjpabook.KYHjpashop.service.MemberService;
import KYHjpabook.KYHjpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("order")
    public String createForm(Model model) {

        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "order/orderForm";
    }

    @PostMapping("order")
    public String order (@RequestParam("memberId") Long memberId,
                         @RequestParam Long itemId,
                         @RequestParam int count) {

        orderService.order(memberId, itemId, count);  // 문제가 있다면 예외가 터질것임.

        return "redirect:/orders";
    }

    // `@ModelAttribute` 를 사용하면 해당 객체가 자동으로 모델 박스에 담김
    @GetMapping("orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        return "order/orderList";
    }

    @PostMapping("orders/{id}/cancel")
    public String orderCancel(@PathVariable("id") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }

}