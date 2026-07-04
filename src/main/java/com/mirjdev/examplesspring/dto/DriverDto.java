package com.mirjdev.examplesspring.dto;

import com.mirjdev.examplesspring.entity.Driver;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

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