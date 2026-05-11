package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.StatisticsDto;
import ch.packops.packopsbackend.service.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // GET /api/statistics
    // Alle eingeloggten User gemaess SecurityConfig
    @GetMapping
    public ResponseEntity<StatisticsDto> getOverviewStatistics() {
        return ResponseEntity.ok(statisticsService.getOverviewStatistics());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}