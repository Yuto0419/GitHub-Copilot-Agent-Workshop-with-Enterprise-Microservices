package com.skishop.sales.mapper;

import com.skishop.sales.dto.response.ShipmentResponse;
import com.skishop.sales.entity.jpa.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Shipment Mapper
 */
@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    /**
     * Convert shipment entity to response DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "orderId", target = "orderId")
    @Mapping(source = "status", target = "status")
    ShipmentResponse toResponse(Shipment shipment);

    /**
     * Convert shipping address entity to response DTO
     */
    ShipmentResponse.ShippingAddressResponse toShippingAddressResponse(Shipment.ShippingAddress shippingAddress);
}
