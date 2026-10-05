package com.hotel.reservation.service;

import com.hotel.reservation.entity.Employee;
import com.hotel.reservation.repository.EmployeeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    // Constructor Injection (A Spring best practice)
    public EmployeeService(EmployeeRepository employeeRepository, AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public void saveEmployee(Employee employee) {
        boolean isNew = employee.getId() == null;
        Employee saved = employeeRepository.save(employee);
        auditLogService.log(actor(), isNew ? "CREATE" : "UPDATE", "Employee", saved.getId(),
                (isNew ? "Created employee: " : "Updated employee: ") + saved.getId());
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
        auditLogService.log(actor(), "DELETE", "Employee", id,
                "Deleted employee id: " + id);
    }

    private String actor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "system";
    }
}