package com.example.smartcampus.dto.schedule;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateScheduleRequest {

    @NotNull @Min(1) @Max(7)
    private Integer dayOfWeek;

    @NotBlank
    private String timeSlot;

    @NotNull
    private Long roomId;

    @NotNull @Min(1)
    private Integer startWeek;

    @NotNull @Min(1)
    private Integer endWeek;
}