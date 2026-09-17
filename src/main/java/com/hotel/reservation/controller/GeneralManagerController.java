package com.hotel.reservation.controller;

import com.hotel.reservation.entity.Employee;
import com.hotel.reservation.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager")
public class GeneralManagerController {

    private final EmployeeService employeeService;

    public GeneralManagerController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // READ: View all employees
    @GetMapping("/dashboard")
    public String showManagerDashboard(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "manager/dashboard";
    }

    // CREATE: Show the form
    @GetMapping("/employee/new")
    public String showCreateForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "manager/employee-form";
    }

    // CREATE & UPDATE: Save data
    @PostMapping("/employee/save")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            return "manager/employee-form";
        }
        employeeService.saveEmployee(employee);
        return "redirect:/manager/dashboard";
    }

    // UPDATE: Show the form with existing data
    @GetMapping("/employee/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        return "manager/employee-form";
    }

    // DELETE: Remove an employee
    @GetMapping("/employee/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/manager/dashboard";
    }
}