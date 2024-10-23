package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {
    @JsonProperty("app")
    String app;
    @JsonProperty("uri")
    String uri;
    @JsonProperty("hits")
    Long hits;
}
