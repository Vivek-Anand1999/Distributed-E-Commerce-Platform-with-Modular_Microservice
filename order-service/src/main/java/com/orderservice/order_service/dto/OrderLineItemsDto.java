package com.orderservice.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemsDto {

    private Long id;
    private BigDecimal price;
    private Integer quantity;
    private String skuCode;
}
