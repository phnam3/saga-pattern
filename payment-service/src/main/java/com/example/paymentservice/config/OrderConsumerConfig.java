package com.example.paymentservice.config;

import com.example.commondtos.dto.OrderRequestDto;
import com.example.commondtos.dto.PaymentRequestDto;
import com.example.commondtos.event.OrderEvent;
import com.example.commondtos.event.constants.OrderStatus;
import com.example.commondtos.event.constants.PaymentStatus;
import com.example.paymentservice.service.PaymentService;
import com.example.paymentservice.service.PaymentStatusPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConsumerConfig {

    private final PaymentService paymentService;

    @RabbitListener(queues = "order_event_queue")
    public void orderEventConsumer(OrderEvent orderEvent){
        //get the user
        //check the balance availability
        //if balance is sufficient -> payment COMPLETED -> deduct amount from DB
        //if balance is not sufficient -> cancel order event and update the amount in DB
        System.out.println(orderEvent);
        if(OrderStatus.ORDER_CREATED.equals(orderEvent.getOrderStatus())){
            paymentService.newOrderEvent(orderEvent);
        } else {
            paymentService.cancelOrderEvent(orderEvent);
        }
    }

}
