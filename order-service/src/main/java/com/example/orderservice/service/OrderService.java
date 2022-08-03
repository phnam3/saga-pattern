package com.example.orderservice.service;

import com.example.commondtos.dto.OrderRequestDto;
import com.example.commondtos.event.constants.OrderStatus;
import com.example.orderservice.entity.PurchaseOrder;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusPublisher orderStatusPublisher;

    /***
     * Create order from the order request dto
     * Save the order into the order database
     * Set order request id = order id (created incrementally by the database)
     * Send message to the order_event_exchange Exchange with order_event_routingKey Routing Key
     */
    @Transactional
    public PurchaseOrder createOrder(OrderRequestDto orderRequestDto) {
        PurchaseOrder order = orderRepository.save(convertDtoToEntity(orderRequestDto));
        orderRequestDto.setOrderId(order.getId());
        //produce rabbitmq event with status order_created
        orderStatusPublisher.publishOrderEvent(orderRequestDto, OrderStatus.ORDER_CREATED);
        return order;
    }

    public List<PurchaseOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    private PurchaseOrder convertDtoToEntity(OrderRequestDto orderRequestDto){
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setProductId(orderRequestDto.getProductId());
        purchaseOrder.setUserId(orderRequestDto.getUserId());
        purchaseOrder.setOrderStatus(OrderStatus.ORDER_CREATED);
        purchaseOrder.setAmount(orderRequestDto.getAmount());
        return purchaseOrder;
    }
}
