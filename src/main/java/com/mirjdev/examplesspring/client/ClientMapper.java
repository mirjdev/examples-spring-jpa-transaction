package com.mirjdev.examplesspring.client;

import com.mirjdev.examplesspring.client.dto.ClientCreateRequest;
import com.mirjdev.examplesspring.client.dto.ClientResponse;
import com.mirjdev.examplesspring.client.dto.ClientUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Маппинг между REST-моделями и сущностью {@link Client}.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Client toEntity(ClientCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Client toEntity(ClientUpdateRequest request);

    ClientResponse toResponse(Client client);

    /**
     * Обновляет существующую сущность значениями из запроса.
     *
     * <p>Идентификатор, версия и дата создания не перезаписываются: версией
     * управляет Hibernate, а дата создания неизменяема.</p>
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    void partialUpdate(Client source, @MappingTarget Client target);
}
