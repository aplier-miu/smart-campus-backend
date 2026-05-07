package com.example.smartcampus.dto.schedule;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateScheduleRequest {

    @NotNull(message = "teachingClassId不能为空")
    private Long teachingClassId;

    @NotNull(message = "dayOfWeek不能为空")
    @Min(value = 1, message = "dayOfWeek最小为1")
    @Max(value = 7, message = "dayOfWeek最大为7")
    private Integer dayOfWeek;

    @NotBlank(message = "timeSlot不能为空")
    private String timeSlot; // 例如 "08:00-10:00"

    @NotNull(message = "roomId不能为空")
    private Long roomId;

    @NotNull(message = "startWeek不能为空")
    @Min(value = 1, message = "startWeek最小为1")
    private Integer startWeek;

    @NotNull(message = "endWeek不能为空")
    @Min(value = 1, message = "endWeek最小为1")
    private Integer endWeek;
}