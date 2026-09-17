package com.hotel.reservation.controller;
import com.hotel.reservation.service.AuditLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    @GetMapping
    public String listLogs(Model model) {
        model.addAttribute("logs", auditLogService.getAllLogs());
        return "admin/audit-logs";
    }
}