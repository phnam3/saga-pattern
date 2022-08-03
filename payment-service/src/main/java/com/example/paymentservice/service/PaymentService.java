package com.example.paymentservice.service;

import com.example.commondtos.dto.OrderRequestDto;
import com.example.commondtos.dto.PaymentRequestDto;
import com.example.commondtos.event.OrderEvent;
import com.example.commondtos.event.PaymentEvent;
import com.example.commondtos.event.constants.PaymentStatus;
import com.example.paymentservice.entity.UserBalance;
import com.example.paymentservice.entity.UserTransaction;
import com.example.paymentservice.repository.UserBalanceRepository;
import com.example.paymentservice.repository.UserTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserBalanceRepository userBalanceRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final PaymentStatusPublisher paymentStatusPublisher;

    @PostConstruct
    public void initUserBalanceInDB() {
        userBalanceRepository.saveAll(
                Stream.of(
                        new UserBalance(1L, 5000),
                        new UserBalance(2L, 3000),
                        new UserBalance(3L, 4200),
                        new UserBalance(4L, 20000),
                        new UserBalance(5L, 900)).collect(Collectors.toList())
        );
    }

    /**
     * get the user
     * check the balance availability
     * if balance is sufficient -> payment COMPLETED -> deduct amount from DB
     * if balance is not sufficient -> cancel order event and update the amount in DB
     */
    @Transactional
    public void newOrderEvent(OrderEvent orderEvent) {
        //Retrieve the order request dto
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();
        //Create the payment request dto for this order
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(orderRequestDto.getOrderId(), orderRequestDto.getUserId(), orderRequestDto.getAmount());

        //Retrieve the user balance
        Optional<UserBalance> userBalance = userBalanceRepository.findById(orderRequestDto.getUserId());
        if (userBalance.isPresent()) {
            userBalance.filter(item -> item.getAmount() > orderRequestDto.getAmount())
                    .map(u -> {
                        //deduct the amount from the balance -> this is the logic behind the committed transaction
                        u.setAmount(u.getAmount() - orderRequestDto.getAmount());
                        //create new user transaction for this order -> this is committed into the database so need to rollback
                        userTransactionRepository.save(new UserTransaction(orderRequestDto.getOrderId(), orderRequestDto.getUserId(), orderRequestDto.getAmount()));
                        return paymentStatusPublisher.publishPaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_COMPLETED);
                    });
        } else {
            paymentStatusPublisher.publishPaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_FAILED);
        }
    }

    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {
        //Retrieve the order request dto
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();
        //Create the payment request dto for this order
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(orderRequestDto.getOrderId(), orderRequestDto.getUserId(), orderRequestDto.getAmount());
        userTransactionRepository.findById(orderRequestDto.getOrderId())
                //if there is a committed transaction
                .ifPresent(userTransaction -> {
                    //delete the newly created, failed transaction
                    userTransactionRepository.delete(userTransaction);
                    userBalanceRepository.findById(userTransaction.getUserId())
                            .ifPresent(userBalance -> {
                                //then update the user balance which been deducted from the transaction
                                userBalance.setAmount(userBalance.getAmount() + userTransaction.getAmount());
                            });
                });
    }
}
