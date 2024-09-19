package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.model.EndpointHit;

@Component
public class EndpointHitMapper {
    public static EndpointHit toEndpointHit(EndpointHitCreateDto endpointHitCreateDto) {
        return EndpointHit.builder()
                .id(endpointHitCreateDto.getId())
                .app(endpointHitCreateDto.getApp())
                .uri(endpointHitCreateDto.getUri())
                .ip(endpointHitCreateDto.getIp())
                .timestamp(endpointHitCreateDto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }
}
