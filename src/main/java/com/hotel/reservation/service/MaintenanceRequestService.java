package com.hotel.reservation.service;

import com.hotel.reservation.entity.MaintenanceRequest;
import com.hotel.reservation.repository.MaintenanceRequestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRepository;
    private final AuditLogService auditLogService;

    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRepository,
                                     AuditLogService auditLogService) {
        this.maintenanceRepository = maintenanceRepository;
        this.auditLogService = auditLogService;
    }

    public List<MaintenanceRequest> getRequests(String status) {
        if (status != null && !status.isBlank()) {
            return maintenanceRepository.findByStatus(status);
        }
        return maintenanceRepository.findAll();
    }

    public MaintenanceRequest getRequestById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance request not found"));
    }

    public MaintenanceRequest createRequest(MaintenanceRequest request) {
        request.setId(null);
        request.setStatus(MaintenanceRequest.STATUS_OPEN);
        MaintenanceRequest saved = maintenanceRepository.save(request);
        auditLogService.log(getCurrentUsername(), "CREATE", "MaintenanceRequest", saved.getId(),
                "Logged maintenance issue for room " + saved.getRoomNumber());
        return saved;
    }

    public MaintenanceRequest updateRequest(Long id, MaintenanceRequest updated) {
        MaintenanceRequest existing = getRequestById(id);
        existing.setRoomNumber(updated.getRoomNumber());
        existing.setIssue(updated.getIssue());
        existing.setReportedBy(updated.getReportedBy());
        if (updated.getStatus() != null && !updated.getStatus().isBlank()) {
            existing.setStatus(updated.getStatus());
        }
        MaintenanceRequest saved = maintenanceRepository.save(existing);
        auditLogService.log(getCurrentUsername(), "UPDATE", "MaintenanceRequest", saved.getId(),
                "Updated maintenance issue for room " + saved.getRoomNumber());
        return saved;
    }

    public MaintenanceRequest updateStatus(Long id, String status) {
        MaintenanceRequest request = getRequestById(id);
        request.setStatus(status);
        MaintenanceRequest saved = maintenanceRepository.save(request);
        auditLogService.log(getCurrentUsername(), "UPDATE", "MaintenanceRequest", saved.getId(),
                "Maintenance status changed to " + status + " for room " + saved.getRoomNumber());
        return saved;
    }

    public void deleteRequest(Long id) {
        MaintenanceRequest request = getRequestById(id);
        maintenanceRepository.delete(request);
        auditLogService.log(getCurrentUsername(), "DELETE", "MaintenanceRequest", id,
                "Deleted maintenance request for room " + request.getRoomNumber());
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "system";
    }
}
