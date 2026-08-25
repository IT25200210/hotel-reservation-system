package com.hotel.reservation.controller;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.service.RoleService;
import com.hotel.reservation.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;
    private final RoleService roleService;
    public AdminUserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/user-form";
    }
    @PostMapping
    public String createUser(@ModelAttribute User user, @RequestParam Long roleId) {
        userService.createUser(user, roleId);
        return "redirect:/admin/users";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/user-form";
    }
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user,
                             @RequestParam Long roleId) {
        userService.updateUser(id, user, roleId);
        return "redirect:/admin/users";
    }
    @GetMapping("/deactivate/{id}")
    public String deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return "redirect:/admin/users";
    }
    @GetMapping("/activate/{id}")
    public String activate(@PathVariable Long id) {
        userService.activateUser(id);
        return "redirect:/admin/users";
    }
}