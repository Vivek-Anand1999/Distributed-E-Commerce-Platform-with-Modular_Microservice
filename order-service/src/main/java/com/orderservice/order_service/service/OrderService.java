package com.orderservice.order_service.service;

import com.orderservice.order_service.dto.InventoryResponse;
import com.orderservice.order_service.dto.OrderLineItemsDto;
import com.orderservice.order_service.dto.OrderRequest;
import com.orderservice.order_service.dto.OrderResponse;
import com.orderservice.order_service.model.Order;
import com.orderservice.order_service.model.OrderLineItems;
import com.orderservice.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Autowired
    public OrderService(WebClient webClient, OrderRepository orderRepository) { // use this or use one annotation @RequiredArgsCons from lombok
        this.webClient = webClient;
        this.orderRepository = orderRepository;
    }

    public void createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDto().stream()
                .map(orderLineItemsDto -> mapToOrderLineItems(orderLineItemsDto)).toList();
        order.setOrderLineItems(orderLineItems);
        List<String> skuCodes = order.getOrderLineItems().stream().map(orderLineItem -> orderLineItem.getSkuCode()).toList();

        InventoryResponse[] inventoryResponsesArray = webClient.get()
                .uri("http://localhost:8083/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        boolean isInventoryPresent = Arrays.stream(inventoryResponsesArray).allMatch(inventoryResponse -> inventoryResponse.isInStock());

        if (isInventoryPresent) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not available.");
        }
    }

    private OrderLineItems mapToOrderLineItems(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> orderResponseList = orders.stream().map(order -> mapToOrderResponse(order)).toList();
        return orderResponseList;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        List<OrderLineItemsDto> lineItemsDto = order.getOrderLineItems().stream().map(orderLineItems -> mapToOrderLineDto(orderLineItems)).toList();
        orderResponse.setOrderLineItemsDto(lineItemsDto);
        return orderResponse;
    }

    private OrderLineItemsDto mapToOrderLineDto(OrderLineItems orderLineItems) {
        OrderLineItemsDto orderLineItemsDto = new OrderLineItemsDto();
        orderLineItemsDto.setId(orderLineItems.getId());
        orderLineItemsDto.setPrice(orderLineItems.getPrice());
        orderLineItemsDto.setQuantity(orderLineItems.getQuantity());
        orderLineItemsDto.setSkuCode(orderLineItems.getSkuCode());
        return orderLineItemsDto;
    }
}
