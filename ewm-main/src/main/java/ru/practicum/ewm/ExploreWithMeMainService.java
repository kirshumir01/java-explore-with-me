package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.client.StatsClientImpl;
import ru.practicum.ewm.client.controller.StatsClientController;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication(scanBasePackages = {"ru.practicum.ewm", "ru.practicum.ewm.statistic-client"})
public class ExploreWithMeMainService {
    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeMainService.class, args);

        StatsClient statsClient = new StatsClientImpl(RestClient.builder()
                .baseUrl("http://localhost:9090")
                .requestFactory(getClientHttpRequestFactory())
                .build());
        StatsClientController controller = new StatsClientController(statsClient);

        EndpointHitCreateDto hit1 = new EndpointHitCreateDto("app1", "/endpoint1", "192.168.1.1", LocalDateTime.now());
        EndpointHitCreateDto hit2 = new EndpointHitCreateDto("app2", "/endpoint1", "192.168.1.2", LocalDateTime.now());
        EndpointHitCreateDto hit3 = new EndpointHitCreateDto("app3", "/endpoint2", "192.168.1.1", LocalDateTime.now());

        controller.saveHit(hit1);
        controller.saveHit(hit2);
        controller.saveHit(hit3);

        List<ViewStatsDto> allStats = controller.getStats(
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(15),
                List.of("/endpoint1", "/endpoint2"),
                false
        );

        List<ViewStatsDto> uniqueStats = statsClient.getStats(
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(15),
                List.of("/endpoint1", "/endpoint2"),
                true
        );

        System.out.println("All stats: " + allStats.size());
        System.out.println("Unique stats: " + uniqueStats.size());
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(10000);
        factory.setConnectTimeout(10000);
        return factory;
    }
}