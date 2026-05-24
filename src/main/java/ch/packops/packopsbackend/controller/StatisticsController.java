package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.StatisticsDto;
import ch.packops.packopsbackend.service.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author David M.
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<StatisticsDto> getOverviewStatistics(@RequestParam(defaultValue = "de") String language) {
        return ResponseEntity.ok(statisticsService.getOverviewStatistics(language));
    }

    @GetMapping("/{processId}")
    public ResponseEntity<StatisticsDto> getProcessStatistics(@PathVariable Long processId, @RequestParam(defaultValue = "de") String language) {
        return ResponseEntity.ok(statisticsService.getProcessStatistics(processId, language));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<StatisticsDto> getProductStatistics(@PathVariable Long productId, @RequestParam(defaultValue = "de") String language) {
        return ResponseEntity.ok(statisticsService.getProductStatistics(productId, language));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}