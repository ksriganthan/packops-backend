package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.StatisticsDto;
import ch.packops.packopsbackend.security.AuthService;
import ch.packops.packopsbackend.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * StatisticsController – Zusätzlicher Endpunkt für das StatisticsDashboard.
 * Dieser Controller ist nicht explizit in der API-Spezifikation definiert,
 * wurde jedoch als Erweiterung implementiert, da das StatisticsDashboard
 * im Frontend eine Gesamtübersicht aller Prozesse benötigt.
 * Die Prozessdetails eines einzelnen Prozesses werden gemäss Spezifikation
 * über GET /api/process/{id} abgerufen (ProcessController).
 *
 * @author Kapischan
 */

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AuthService authService;

    public StatisticsController(StatisticsService statisticsService,
                                AuthService authService) {
        this.statisticsService = statisticsService;
        this.authService = authService;
    }

    // GET /api/statistics
    @GetMapping
    public ResponseEntity<?> getOverviewStatistics(@RequestParam String token) {
        try {
            authService.authenticate(token);
            StatisticsDto stats = statisticsService.getOverviewStatistics();
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }
}
