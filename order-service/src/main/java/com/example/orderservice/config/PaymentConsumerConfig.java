package com.example.orderservice.config;

import com.example.commondtos.event.OrderEvent;
import com.example.commondtos.event.PaymentEvent;
import com.example.commondtos.event.constants.OrderStatus;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class PaymentConsumerConfig {

    private final OrderStatusUpdateHandler handler;

    @RabbitListener(queues = "payment_event_queue")
    public void orderEventConsumer(PaymentEvent paymentEvent){
        //listen payment event message
        //check the payment status
        //if payment status completed -> complete the order
        //else if payment status failed -> cancel the order
        System.out.println(paymentEvent);
        handler.updateOrder(paymentEvent.getPaymentRequestDto().getOrderId(), paymentEvent.getPaymentStatus());

    }

}
