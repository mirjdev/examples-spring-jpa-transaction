package com.mirjdev.examplesspring.dto;

import com.mirjdev.examplesspring.entity.Driver;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for {@link Driver}
 */
@Builder(toBuilder = true)
@Value
public class DriverDto implements Serializable {
    Long id;
    @NotNull
    @NotEmpty
    @NotBlank
    String fio;
    String comment;
    String driverLicense;
}