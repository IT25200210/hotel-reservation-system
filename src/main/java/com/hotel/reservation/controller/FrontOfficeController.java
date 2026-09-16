package com.hotel.reservation.controller;

import com.hotel.reservation.service.DeskOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/frontoffice")
public class FrontOfficeController {
    private final DeskOperations desk;

    public FrontOfficeController(DeskOperations desk) { this.desk = desk; }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("rooms", desk.rooms());
        model.addAttribute("reservations", desk.reservations());
        model.addAttribute("stays", desk.stays());
        return "frontoffice/dashboard";
    }

    @PostMapping("/check-in")
    public String checkIn(@RequestParam Long roomId, @RequestParam String guest,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                          LocalDate departure, @RequestParam BigDecimal rate) {
        desk.checkIn(roomId, guest, departure, rate);
        return "redirect:/frontoffice";
    }

    @PostMapping("/{id}/check-out")
    public String checkOut(@PathVariable Long id) {
        desk.checkOut(id);
        return "redirect:/frontoffice";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleError(ResponseStatusException ex, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getReason() == null ? "Action rejected" : ex.getReason());
        return "redirect:/frontoffice";
    }

    @PostMapping("/reservations/{id}/check-in")
    public String arriveReservation(@PathVariable Long id) {
        desk.arriveReservation(id);
        return "redirect:/frontoffice";
    }
}
