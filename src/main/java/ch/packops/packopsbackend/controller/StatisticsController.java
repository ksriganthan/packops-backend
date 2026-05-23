package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.StatisticsDto;
import ch.packops.packopsbackend.service.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
/**
 * @author David M.
 */
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<StatisticsDto> getOverviewStatistics() {
        return ResponseEntity.ok(statisticsService.getOverviewStatistics());
    }

    @GetMapping("/{processId}")
    public ResponseEntity<StatisticsDto> getProcessStatistics(@PathVariable Long processId) {
        return ResponseEntity.ok(statisticsService.getProcessStatistics(processId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<StatisticsDto> getProductStatistics(@PathVariable Long productId) {
        return ResponseEntity.ok(statisticsService.getProductStatistics(productId));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}