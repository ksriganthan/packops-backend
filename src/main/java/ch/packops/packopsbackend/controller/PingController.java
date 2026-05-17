package ch.packops.packopsbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * @author Teodor Glisic
 */

@RestController
@RequestMapping("/api/ping")
public class PingController {

    @GetMapping
    public ResponseEntity<Map<String, Boolean>> pingServer() {
        return ResponseEntity.status(200).body(Collections.singletonMap("ping", true));
    }
}
