package com.hotel.reservation.controller;

import com.hotel.reservation.entity.DeskRoom;
import com.hotel.reservation.entity.Employee;
import com.hotel.reservation.service.EmployeeService;
import com.hotel.reservation.service.RoomService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager")
public class GeneralManagerController {

    private final EmployeeService employeeService;
    private final RoomService roomService;

    public GeneralManagerController(EmployeeService employeeService,
                                    RoomService roomService) {

        this.employeeService = employeeService;
        this.roomService = roomService;
    }

    // =========================
    // EMPLOYEE MANAGEMENT
    // =========================

    @GetMapping("/dashboard")
    public String showManagerDashboard(Model model) {

        model.addAttribute(
                "employees",
                employeeService.getAllEmployees()
        );

        return "manager/dashboard";
    }

    @GetMapping("/employee/new")
    public String showCreateForm(Model model) {

        model.addAttribute(
                "employee",
                new Employee()
        );

        return "manager/employee-form";
    }

    @PostMapping("/employee/save")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result) {

        if (result.hasErrors()) {
            return "manager/employee-form";
        }

        employeeService.saveEmployee(employee);

        return "redirect:/manager/dashboard";
    }

    @GetMapping("/employee/edit/{id}")
    public String showUpdateForm(
            @PathVariable("id") Long id,
            Model model) {

        Employee employee =
                employeeService.getEmployeeById(id);

        model.addAttribute(
                "employee",
                employee
        );

        return "manager/employee-form";
    }

    @GetMapping("/employee/delete/{id}")
    public String deleteEmployee(
            @PathVariable("id") Long id) {

        employeeService.deleteEmployee(id);

        return "redirect:/manager/dashboard";
    }


    // =========================
    // ROOM MANAGEMENT
    // =========================

    @GetMapping("/rooms")
    public String showRooms(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Show maximum 10 rooms per page
        int pageSize = 10;

        Page<DeskRoom> roomPage =
                roomService.getRoomsPaginated(page, pageSize);

        // Rooms displayed in table
        model.addAttribute(
                "rooms",
                roomPage.getContent()
        );

        // Pagination information
        model.addAttribute(
                "currentPage",
                roomPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                roomPage.getTotalPages()
        );

        model.addAttribute(
                "totalItems",
                roomPage.getTotalElements()
        );

        return "manager/rooms";
    }


    @GetMapping("/room/new")
    public String showCreateRoomForm(Model model) {

        model.addAttribute(
                "room",
                new DeskRoom()
        );

        model.addAttribute(
                "roomTypes",
                DeskRoom.RoomType.values()
        );

        return "manager/room-form";
    }


    @GetMapping("/room/edit/{id}")
    public String showEditRoomForm(
            @PathVariable("id") Long id,
            Model model) {

        model.addAttribute(
                "room",
                roomService.getRoomById(id)
        );

        model.addAttribute(
                "roomTypes",
                DeskRoom.RoomType.values()
        );

        return "manager/room-form";
    }


    @PostMapping("/room/save")
    public String saveRoom(
            @Valid @ModelAttribute("room") DeskRoom room,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "roomTypes",
                    DeskRoom.RoomType.values()
            );

            return "manager/room-form";
        }

        try {

            roomService.saveRoom(room);

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "roomTypes",
                    DeskRoom.RoomType.values()
            );

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "manager/room-form";
        }

        return "redirect:/manager/rooms";
    }


    @GetMapping("/room/delete/{id}")
    public String deleteRoom(
            @PathVariable("id") Long id) {

        roomService.deactivateRoom(id);

        return "redirect:/manager/rooms";
    }
}