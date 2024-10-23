package ru.practicum.ewm.location.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @Min(value = -90)
    @Max(value = 90)
    private Float lat;
    @Min(value = -180)
    @Max(value = 180)
    private Float lon;
}
