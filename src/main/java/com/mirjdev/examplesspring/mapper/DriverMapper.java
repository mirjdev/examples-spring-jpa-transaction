package com.mirjdev.examplesspring.mapper;

import com.mirjdev.examplesspring.dto.DriverDto;
import com.mirjdev.examplesspring.entity.Driver;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

//JPA Buddy - отличный плагин, в том числе для генерации мапперов, этот маппер генерировался, но вносились небольшие правки
//MapStruct support - еще один отличный плагин
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Driver toEntity(DriverDto driverDto);

    DriverDto toDto(Driver driver);

    @Mapping(target = "version", ignore = true)
    //обычно используется NullValuePropertyMappingStrategy.IGNORE, если с фронта не приходят поля, то не обновляем, останутся такие как в энтити
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    Driver partialUpdate(DriverDto driverDto, @MappingTarget Driver driver);
}