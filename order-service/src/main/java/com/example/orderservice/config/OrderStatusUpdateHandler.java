package com.example.orderservice.config;

import com.example.commondtos.dto.OrderRequestDto;
import com.example.commondtos.event.PaymentEvent;
import com.example.commondtos.event.constants.OrderStatus;
import com.example.commondtos.event.constants.PaymentStatus;
import com.example.orderservice.entity.PurchaseOrder;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderStatusPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.transaction.Transactional;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class OrderStatusUpdateHandler {

    private final OrderRepository orderRepository;
    private final OrderStatusPublisher orderStatusPublisher;

    //find the order id and proceed to appropriate actions
    @Transactional
    public void updateOrder(Long orderId, PaymentStatus paymentStatus){
        PurchaseOrder purchaseOrder = orderRepository.findById(orderId).get();
        boolean isPaymentCompleted = PaymentStatus.PAYMENT_COMPLETED.equals(paymentStatus);
        OrderStatus orderStatus = isPaymentCompleted? OrderStatus.ORDER_COMPLETED:OrderStatus.ORDER_CANCELLED;
        purchaseOrder.setOrderStatus(orderStatus);
        if(!isPaymentCompleted){
            orderStatusPublisher.publishOrderEvent(convertEntityToDto(purchaseOrder), orderStatus);
        }
        orderRepository.save(purchaseOrder);
    }

    public OrderRequestDto convertEntityToDto(PurchaseOrder purchaseOrder) {
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderId(purchaseOrder.getId());
        orderRequestDto.setUserId(purchaseOrder.getUserId());
        orderRequestDto.setAmount(purchaseOrder.getAmount());
        orderRequestDto.setProductId(purchaseOrder.getProductId());
        return orderRequestDto;
    }

}
