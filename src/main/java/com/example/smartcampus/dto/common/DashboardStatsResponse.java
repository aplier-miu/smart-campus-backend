package com.example.smartcampus.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long studentCount;
    private Long courseCount;
    private Long teachingClassCount;
}