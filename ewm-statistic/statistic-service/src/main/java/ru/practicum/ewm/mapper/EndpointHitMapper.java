package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.model.EndpointHit;

@Component
public class EndpointHitMapper {
    public static EndpointHit toEndpointHit(EndpointHitCreateDto endpointHitCreateDto) {
        return EndpointHit.builder()
                .app(endpointHitCreateDto.getApp())
                .uri(endpointHitCreateDto.getUri())
                .ip(endpointHitCreateDto.getIp())
                .timestamp(endpointHitCreateDto.getTimestamp())
                .build();
    }
}
