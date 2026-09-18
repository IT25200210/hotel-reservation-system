package com.hotel.reservation.controller;

import com.hotel.reservation.service.DeskOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.math.BigDecimal;

@Controller
@RequestMapping("/reservations")
public class ReservationsController {
    private final DeskOperations desk;

    public ReservationsController(DeskOperations desk) { this.desk = desk; }

    // READ - dashboard
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("rooms", desk.rooms());
        model.addAttribute("reservations", desk.reservations());
        return "reservations/dashboard";
    }

    // CREATE
    @PostMapping
    public String create(@RequestParam Long roomId, @RequestParam String guest,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure,
                         @RequestParam BigDecimal rate,
                         RedirectAttributes redirectAttributes) {
        try {
            desk.createReservation(roomId, guest, arrival, departure, rate);
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
        }
        return "redirect:/reservations";
    }

    // UPDATE - show edit form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", desk.reservations().stream()
                .filter(r -> r.getId().equals(id)).findFirst().orElseThrow());
        model.addAttribute("rooms", desk.rooms());
        return "reservations/edit";
    }

    // UPDATE - save changes
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @RequestParam Long roomId, @RequestParam String guest,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure,
                         @RequestParam BigDecimal rate,
                         RedirectAttributes redirectAttributes) {
        try {
            desk.modifyReservation(id, roomId, guest, arrival, departure, rate);
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
            return "redirect:/reservations/" + id + "/edit";
        }
        return "redirect:/reservations";
    }

    // DELETE - cancel
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            desk.cancelReservation(id);
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
        }
        return "redirect:/reservations";
    }
}
