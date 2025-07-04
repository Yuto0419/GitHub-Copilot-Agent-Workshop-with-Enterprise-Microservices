package com.skishop.sales.mapper;

import com.skishop.sales.dto.response.ReturnResponse;
import com.skishop.sales.entity.jpa.Return;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Return mapper
 */
@Mapper(componentModel = "spring")
public interface ReturnMapper {

    /**
     * Convert Return entity to Response DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "orderId", target = "orderId")
    @Mapping(source = "orderItemId", target = "orderItemId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "reason", target = "reason")
    @Mapping(target = "orderItemInfo", ignore = true)
    ReturnResponse toResponse(Return returnEntity);
}
