package com.hotel.reservation.controller;

import com.hotel.reservation.entity.HousekeepingTask;
import com.hotel.reservation.entity.MaintenanceRequest;
import com.hotel.reservation.service.HousekeepingTaskService;
import com.hotel.reservation.service.MaintenanceRequestService;
import com.hotel.reservation.service.RoomCleaningStatusService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/housekeeping")
public class HousekeepingController {

    private final HousekeepingTaskService taskService;
    private final RoomCleaningStatusService roomStatusService;
    private final MaintenanceRequestService maintenanceService;

    public HousekeepingController(HousekeepingTaskService taskService,
                                  RoomCleaningStatusService roomStatusService,
                                  MaintenanceRequestService maintenanceService) {
        this.taskService = taskService;
        this.roomStatusService = roomStatusService;
        this.maintenanceService = maintenanceService;
    }

    // ---------- Housekeeping tasks ----------

    @GetMapping({"", "/tasks"})
    public String listTasks(@RequestParam(required = false) String status,
                            @RequestParam(required = false) String roomNumber,
                            Model model) {
        model.addAttribute("tasks", taskService.getTasks(status, roomNumber));
        model.addAttribute("statusFilter", status);
        model.addAttribute("roomFilter", roomNumber);
        model.addAttribute("pendingCount", taskService.getTasks("PENDING", null).size());
        model.addAttribute("inProgressCount", taskService.getTasks("IN_PROGRESS", null).size());
        model.addAttribute("completedCount", taskService.getTasks("COMPLETED", null).size());
        return "housekeeping/tasks";
    }

    @GetMapping("/tasks/new")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new HousekeepingTask());
        return "housekeeping/task-form";
    }

    @PostMapping("/tasks")
    public String createTask(@ModelAttribute HousekeepingTask task) {
        taskService.createTask(task);
        return "redirect:/housekeeping/tasks";
    }

    @GetMapping("/tasks/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.getTaskById(id));
        return "housekeeping/task-form";
    }

    @PostMapping("/tasks/update/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute HousekeepingTask task) {
        taskService.updateTask(id, task);
        return "redirect:/housekeeping/tasks";
    }

    @GetMapping("/tasks/status/{id}/{status}")
    public String updateTaskStatus(@PathVariable Long id, @PathVariable String status) {
        taskService.updateTaskStatus(id, status);
        return "redirect:/housekeeping/tasks";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/housekeeping/tasks";
    }

    // ---------- Room cleaning status board ----------

    @GetMapping("/rooms")
    public String roomStatusBoard(Model model) {
        var rooms = roomStatusService.getAllRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("dirtyCount", rooms.stream().filter(r -> "DIRTY".equals(r.getStatus())).count());
        model.addAttribute("inProgressCount", rooms.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).count());
        model.addAttribute("cleanCount", rooms.stream().filter(r -> "CLEAN".equals(r.getStatus())).count());
        model.addAttribute("inspectedCount", rooms.stream().filter(r -> "INSPECTED".equals(r.getStatus())).count());
        return "housekeeping/rooms";
    }

    @PostMapping("/rooms/add")
    public String addRoom(@RequestParam String roomNumber, RedirectAttributes redirectAttributes) {
        try {
            roomStatusService.createRoom(roomNumber);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/housekeeping/rooms";
    }

    @GetMapping("/rooms/status/{id}/{status}")
    public String updateRoomStatus(@PathVariable Long id, @PathVariable String status) {
        roomStatusService.updateStatus(id, status);
        return "redirect:/housekeeping/rooms";
    }

    // ---------- Maintenance requests ----------

    @GetMapping("/maintenance")
    public String listMaintenance(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("requests", maintenanceService.getRequests(status));
        model.addAttribute("statusFilter", status);
        model.addAttribute("openCount", maintenanceService.getRequests("OPEN").size());
        model.addAttribute("inProgressCount", maintenanceService.getRequests("IN_PROGRESS").size());
        model.addAttribute("resolvedCount", maintenanceService.getRequests("RESOLVED").size());
        return "housekeeping/maintenance";
    }

    @GetMapping("/maintenance/new")
    public String showMaintenanceForm(Model model, Authentication authentication) {
        MaintenanceRequest request = new MaintenanceRequest();
        if (authentication != null) {
            request.setReportedBy(authentication.getName());
        }
        model.addAttribute("request", request);
        return "housekeeping/maintenance-form";
    }

    @PostMapping("/maintenance")
    public String createMaintenance(@ModelAttribute MaintenanceRequest request) {
        maintenanceService.createRequest(request);
        return "redirect:/housekeeping/maintenance";
    }

    @GetMapping("/maintenance/edit/{id}")
    public String showEditMaintenanceForm(@PathVariable Long id, Model model) {
        model.addAttribute("request", maintenanceService.getRequestById(id));
        return "housekeeping/maintenance-form";
    }

    @PostMapping("/maintenance/update/{id}")
    public String updateMaintenance(@PathVariable Long id, @ModelAttribute MaintenanceRequest request) {
        maintenanceService.updateRequest(id, request);
        return "redirect:/housekeeping/maintenance";
    }

    @GetMapping("/maintenance/status/{id}/{status}")
    public String updateMaintenanceStatus(@PathVariable Long id, @PathVariable String status) {
        maintenanceService.updateStatus(id, status);
        return "redirect:/housekeeping/maintenance";
    }

    @GetMapping("/maintenance/delete/{id}")
    public String deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteRequest(id);
        return "redirect:/housekeeping/maintenance";
    }

    // ---------- Visual Analytics Dashboard (Chart.js) ----------

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var rooms = roomStatusService.getAllRooms();
        long dirtyRooms = rooms.stream().filter(r -> "DIRTY".equals(r.getStatus())).count();
        long inProgressRooms = rooms.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).count();
        long cleanRooms = rooms.stream().filter(r -> "CLEAN".equals(r.getStatus())).count();
        long inspectedRooms = rooms.stream().filter(r -> "INSPECTED".equals(r.getStatus())).count();

        int pendingTasks = taskService.getTasks("PENDING", null).size();
        int inProgressTasks = taskService.getTasks("IN_PROGRESS", null).size();
        int completedTasks = taskService.getTasks("COMPLETED", null).size();

        int openMaintenance = maintenanceService.getRequests("OPEN").size();
        int inProgressMaintenance = maintenanceService.getRequests("IN_PROGRESS").size();
        int resolvedMaintenance = maintenanceService.getRequests("RESOLVED").size();

        model.addAttribute("totalRooms", rooms.size());
        model.addAttribute("dirtyRooms", dirtyRooms);
        model.addAttribute("inProgressRooms", inProgressRooms);
        model.addAttribute("cleanRooms", cleanRooms);
        model.addAttribute("inspectedRooms", inspectedRooms);

        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("inProgressTasks", inProgressTasks);
        model.addAttribute("completedTasks", completedTasks);

        model.addAttribute("openMaintenance", openMaintenance);
        model.addAttribute("inProgressMaintenance", inProgressMaintenance);
        model.addAttribute("resolvedMaintenance", resolvedMaintenance);

        model.addAttribute("recentTasks", taskService.getTasks(null, null).stream().limit(5).toList());
        model.addAttribute("recentMaintenance", maintenanceService.getRequests(null).stream().limit(5).toList());

        return "housekeeping/dashboard";
    }
}

