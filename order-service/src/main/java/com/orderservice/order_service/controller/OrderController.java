package com.orderservice.order_service.controller;

import com.orderservice.order_service.dto.OrderRequest;
import com.orderservice.order_service.dto.OrderResponse;
import com.orderservice.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public String placeOrder(@RequestBody OrderRequest orderRequest){
        orderService.createOrder(orderRequest);
        return "Order Placed Successfully";
    }

    @GetMapping
    public List<OrderResponse> getAllOrder(){
        return orderService.getAllOrders();
    }
}
