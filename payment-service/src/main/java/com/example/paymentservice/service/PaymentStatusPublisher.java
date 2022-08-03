package com.example.paymentservice.service;

import com.example.commondtos.dto.PaymentRequestDto;
import com.example.commondtos.event.PaymentEvent;
import com.example.commondtos.event.constants.PaymentStatus;
import com.example.paymentservice.config.MQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentStatusPublisher {

    private final RabbitTemplate template;

    public PaymentEvent publishPaymentEvent(PaymentRequestDto paymentRequestDto, PaymentStatus paymentStatus){
        PaymentEvent paymentEvent = new PaymentEvent(paymentRequestDto, paymentStatus);
        template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, paymentEvent);
        return paymentEvent;
    }

}
