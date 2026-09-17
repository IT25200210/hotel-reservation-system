package com.hotel.reservation.controller;

import com.hotel.reservation.service.DeskOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;

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
                          LocalDate departure, @RequestParam BigDecimal rate,
                          @RequestParam(defaultValue = "false") boolean identityVerified) {
        if (!identityVerified) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Guest identity must be verified before check-in");
        }
        desk.checkIn(roomId, guest, departure, rate);
        return "redirect:/frontoffice";
    }

    @PostMapping("/{id}/check-out")
    public String checkOut(@PathVariable Long id) {
        desk.checkOut(id);
        return "redirect:/frontoffice";
    }

    @PostMapping("/reservations/{id}/check-in")
    public String arriveReservation(@PathVariable Long id,
                                    @RequestParam(required = false) Long roomId,
                                    @RequestParam(defaultValue = "false") boolean identityVerified) {
        if (!identityVerified) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Guest identity must be verified before check-in");
        }
        desk.arriveReservation(id, roomId);
        return "redirect:/frontoffice";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleError(ResponseStatusException ex, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getReason() == null ? "Action rejected" : ex.getReason());
        return "redirect:/frontoffice";
    }
}