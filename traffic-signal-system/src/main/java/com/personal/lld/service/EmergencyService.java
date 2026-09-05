package com.personal.lld.service;

import com.personal.lld.domain.EmergencyRequest;
import com.personal.lld.domain.Intersection;
import com.personal.lld.domain.enums.Direction;
import com.personal.lld.repository.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyRepository emergencyRepository;
    private final IntersectionService intersectionService;

    @PostConstruct
    public void init() {
        log.info("EmergencyService initialized");
    }

    public void requestEmergency(int intersectionId, Direction direction, int duration) {
        // Check if intersection exists
        if (intersectionService.getIntersection(intersectionId) == null) {
            log.warn("Cannot request emergency: Intersection not found: {}", intersectionId);
            return;
        }

        // Create emergency request
        int requestId = emergencyRepository.getNextId();
        EmergencyRequest request = new EmergencyRequest(requestId, intersectionId, direction, duration);
        emergencyRepository.save(request);

        // Pause the automatic cycle
        intersectionService.pauseCycle(intersectionId);

        // Set all signals to RED using emergency transition
        intersectionService.emergencySetAllSignalsToRed(intersectionId);

        // Set emergency direction to GREEN
        intersectionService.setSignalToGreen(intersectionId, direction);

        // Update intersection emergency mode
        Intersection intersection = intersectionService.getIntersection(intersectionId);
        if (intersection != null) {
            intersection.setEmergencyMode(true);
            intersection.setEmergencyDirection(direction);
        }

        log.info("Emergency request processed: {} for intersection {}, direction {}, duration {}s",
                requestId, intersectionId, direction, duration);

        // In a real implementation, this would schedule the emergency to end after duration
        log.debug("TODO: Schedule emergency end after {} seconds", duration);
    }

    public void endEmergency(int intersectionId) {
        EmergencyRequest activeEmergency = emergencyRepository.getActiveEmergency(intersectionId);
        if (activeEmergency == null) {
            log.warn("No active emergency found for intersection: {}", intersectionId);
            return;
        }

        // Set all signals to RED using emergency transition
        intersectionService.emergencySetAllSignalsToRed(intersectionId);

        // Deactivate emergency request
        emergencyRepository.updateStatus(activeEmergency.getId(), false);

        // Update intersection emergency mode
        Intersection intersection = intersectionService.getIntersection(intersectionId);
        if (intersection != null) {
            intersection.setEmergencyMode(false);
            intersection.setEmergencyDirection(null);
        }

        // Resume the cycle from where it was paused
        intersectionService.resumeCycle(intersectionId);

        log.info("Emergency ended for intersection: {}", intersectionId);
    }

    public EmergencyRequest getActiveEmergency(int intersectionId) {
        return emergencyRepository.getActiveEmergency(intersectionId);
    }

    public void cleanupExpiredEmergencies() {
        emergencyRepository.removeExpiredRequests();
        log.info("Expired emergency requests cleaned up");
    }
}