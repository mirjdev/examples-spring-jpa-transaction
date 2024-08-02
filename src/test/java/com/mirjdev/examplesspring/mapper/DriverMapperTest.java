package com.mirjdev.examplesspring.mapper;

import com.mirjdev.examplesspring.dto.DriverDto;
import com.mirjdev.examplesspring.entity.Driver;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Spy;

import static com.mirjdev.examplesspring.service.impl.IsolationLevelFacadeImpl.FIO_VER_2;
import static com.mirjdev.examplesspring.service.impl.IsolationLevelFacadeImpl.FIO_VER_3;
import static org.assertj.core.api.Assertions.assertThat;

class DriverMapperTest {

    @Spy
    private DriverMapper mapper = Mappers.getMapper(DriverMapper.class);

    private final EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters()
            .collectionSizeRange(1, 1)
            .stringLengthRange(3, 3));

    @Test
    void toEntity() {
        DriverDto driverDto = easyRandom.nextObject(DriverDto.class);
        Driver entity = mapper.toEntity(driverDto);

        assertThat(entity)
                .usingRecursiveComparison()
                .ignoringFields("id", "version")
                .isEqualTo(driverDto);

        assertThat(entity.getId()).isNull();
    }

    @Test
    void toDto() {
        Driver driver = easyRandom.nextObject(Driver.class);
        DriverDto driverDto = mapper.toDto(driver);

        assertThat(driverDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "version")
                .isEqualTo(driver);
    }

    @Test
    void partialUpdate() {
        Driver driver = easyRandom.nextObject(Driver.class);
        driver.setFio(FIO_VER_2);

        DriverDto driverDto = easyRandom.nextObject(DriverDto.class)
                .toBuilder()
                .fio(FIO_VER_3) // update
                .comment(null)  // set to null
                .build();
        Driver actual = mapper.partialUpdate(driverDto, driver);

        assertThat(driverDto)
                .usingRecursiveComparison()
                .ignoringFields("fio", "comment")
                .isEqualTo(driver);

        assertThat(actual.getFio()).isEqualTo(FIO_VER_3);
        assertThat(actual.getComment()).isNull();
    }
}