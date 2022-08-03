package com.example.orderservice.service;

import com.example.commondtos.dto.OrderRequestDto;
import com.example.commondtos.event.OrderEvent;
import com.example.commondtos.event.constants.OrderStatus;
import com.example.orderservice.config.MQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStatusPublisher {

    private final RabbitTemplate template;

    public OrderEvent publishOrderEvent(OrderRequestDto orderRequestDto, OrderStatus orderStatus){
        OrderEvent orderEvent = new OrderEvent(orderRequestDto, orderStatus);
        template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, orderEvent);
        return orderEvent;
    }
}
