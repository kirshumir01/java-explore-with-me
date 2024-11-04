package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitCreateDto {
    @NotBlank
    @JsonProperty("app")
    String app;
    @NotBlank
    @JsonProperty("uri")
    String uri;
    @NotBlank
    // IPv4: 192.168.1.1
    // IPv6: 0:0:0:0:0:0:0:1
    // IPv6-short: ::1
    @Pattern(regexp = "^(?:(?:\\d{1,3}\\.){3}\\d{1,3}|(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|::1|(?:[0-9a-fA-F]{1,4}:){1,7}:)$")
    @JsonProperty("ip")
    String ip;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("timestamp")
    LocalDateTime timestamp;
}
