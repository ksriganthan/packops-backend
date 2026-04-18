package ch.packops.packopsbackend.service;

import org.springframework.stereotype.Service;

@Service
public class LoggingService {

    public void logInfo(String message) {
        // TODO: implement
    }

    public void logProcessEvent(Long processId, String message) {
        // TODO: implement
    }

    public void logDeadlock(Long processId, Integer bucketNr) {
        // TODO: implement
    }
}