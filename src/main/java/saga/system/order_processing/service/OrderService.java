package saga.system.order_processing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import saga.system.order_processing.dto.*;
import saga.system.order_processing.model.Item;
import saga.system.order_processing.model.Order;
import saga.system.order_processing.model.OrderItem;
import saga.system.order_processing.model.User;
import saga.system.order_processing.repository.ItemRepository;
import saga.system.order_processing.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Item not found: " + itemRequest.getItemId()));

            if (item.getStockedQty() < itemRequest.getQty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Insufficient stock for item: " + item.getName());
            }

            item.setStockedQty(item.getStockedQty() - itemRequest.getQty());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setPurchasePrice(item.getPrice());
            orderItem.setQty(itemRequest.getQty());

            order.getOrderItems().add(orderItem);
            totalPrice = totalPrice.add(item.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQty())));
        }

        order.setPrice(totalPrice);
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(User user) {
        return orderRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(oi -> new OrderItemResponse(oi.getItem().getName(), oi.getPurchasePrice(), oi.getQty()))
                .toList();
        return new OrderResponse(order.getId(), items, order.getPrice(), order.getCreatedAt());
    }
}
