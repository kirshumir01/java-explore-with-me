package ru.practicum.ewm.client;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatsClientTest {
    @Autowired
    private StatsClient statsClient;

    @Test
    public void testSaveHitsAndRetrieveStats() throws InterruptedException {
        statsClient.saveHit(new EndpointHitCreateDto("app1", "/endpoint1", "192.168.1.1", LocalDateTime.now()));
        statsClient.saveHit(new EndpointHitCreateDto("app1", "/endpoint1", "192.168.1.2", LocalDateTime.now()));
        statsClient.saveHit(new EndpointHitCreateDto("app1", "/endpoint2", "192.168.1.1", LocalDateTime.now()));

        Thread.sleep(1000);

        List<ViewStatsDto> allStats = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/endpoint1", "/endpoint2"),
                false
        );
        assertNotNull(allStats);
        assertFalse(allStats.isEmpty());
        System.out.println("All stats: " + allStats);

        List<ViewStatsDto> uniqueStats = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/endpoint1", "/endpoint2"),
                true
        );
        assertNotNull(uniqueStats);
        assertFalse(uniqueStats.isEmpty());
        System.out.println("Unique stats: " + uniqueStats);
    }
}