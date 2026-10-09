package com.hotel.reservation.controller;

import com.hotel.reservation.entity.DeskRoom;
import com.hotel.reservation.entity.DeskReservation;
import com.hotel.reservation.entity.DeskRoom;
import com.hotel.reservation.service.GuestBookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/book")
public class GuestBookingController {

    private final GuestBookingService guests;

    public GuestBookingController(GuestBookingService guests) {
        this.guests = guests;
    }

    @GetMapping
    public String page(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure,
            @RequestParam(required = false) DeskRoom.RoomType type,
            @RequestParam(required = false) String lookupRef,
            @RequestParam(required = false) String receiptRef,
            Model model) {

        // Create session early: Thymeleaf th:action needs a CSRF token which
        // creates a session. Without this, rendering the large rooms table
        // commits the response before the booking form is processed, causing
        // "Cannot create a session after the response has been committed".
        request.getSession(true);

        List<DeskRoom> rooms = guests.listRooms(type);
        model.addAttribute("rooms", rooms);
        model.addAttribute("arrival", arrival);
        model.addAttribute("departure", departure);
        model.addAttribute("types", DeskRoom.RoomType.values());
        model.addAttribute("selectedType", type);

        if (arrival != null && departure != null && departure.isAfter(arrival)) {
            long nights = ChronoUnit.DAYS.between(arrival, departure);
            model.addAttribute("nights", nights);

            Map<Long, Boolean> availability = new HashMap<>();
            Map<Long, BigDecimal> totals = new HashMap<>();

            for (DeskRoom room : rooms) {
                boolean free = false;
                try {
                    free = guests.isAvailable(room.getId(), arrival, departure)
                            && !(arrival.equals(LocalDate.now()) && room.isOccupied());
                } catch (ResponseStatusException ignored) {
                    free = false;
                }
                availability.put(room.getId(), free);
                totals.put(room.getId(),
                        room.getNightlyRate().multiply(BigDecimal.valueOf(nights)));
            }

            model.addAttribute("availability", availability);
            model.addAttribute("totals", totals);
        }

        if (lookupRef != null && !lookupRef.isBlank()) {
            String ref = lookupRef.trim();
            model.addAttribute("lookupRef", ref);
            try {
                DeskReservation managed = guests.lookup(ref);
                model.addAttribute("managed", managed);
            } catch (ResponseStatusException ex) {
                model.addAttribute("lookupError",
                        ex.getReason() == null ? "Booking not found" : ex.getReason());
            }
        }

        if (receiptRef != null && !receiptRef.isBlank()) {
            try {
                DeskReservation receipt = guests.lookup(receiptRef.trim());
                model.addAttribute("receipt", receipt);
                model.addAttribute("showReceipt", true);
            } catch (ResponseStatusException ignored) {
                // unknown ref -> no popup
            }
        }

        return "book";
    }

    @PostMapping
    public String create(@RequestParam Long roomId,
                         @RequestParam String guest,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure,
                         RedirectAttributes flash) {
        try {
            DeskReservation created = guests.create(roomId, guest, arrival, departure);
            flash.addFlashAttribute("success", "Booking confirmed");
            return "redirect:/book?receiptRef=" + created.getReference()
                    + "&arrival=" + arrival + "&departure=" + departure;
        } catch (ResponseStatusException ex) {
            flash.addFlashAttribute("error",
                    ex.getReason() == null ? "Booking failed" : ex.getReason());
            return "redirect:/book?arrival=" + arrival + "&departure=" + departure;
        }
    }

    @PostMapping("/lookup")
    public String lookup(@RequestParam String reference,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure,
                         @RequestParam(required = false) DeskRoom.RoomType type) {
        String dates = "";
        if (arrival != null && departure != null) {
            dates = "&arrival=" + arrival + "&departure=" + departure;
        }
        if (type != null) {
            dates += "&type=" + type;
        }
        return "redirect:/book?lookupRef=" + reference.trim() + dates + "#manage";
    }

    @PostMapping("/{ref}/edit")
    public String update(@PathVariable String ref,
                         @RequestParam Long roomId,
                         @RequestParam String guest,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure,
                         RedirectAttributes flash) {
        try {
            guests.modify(ref, roomId, guest, arrival, departure);
            flash.addFlashAttribute("success", "Booking updated");
        } catch (ResponseStatusException ex) {
            flash.addFlashAttribute("error",
                    ex.getReason() == null ? "Update failed" : ex.getReason());
        }
        return "redirect:/book?lookupRef=" + ref.trim() + "#manage";
    }

    @PostMapping("/{ref}/cancel")
    public String cancel(@PathVariable String ref, RedirectAttributes flash) {
        try {
            guests.cancel(ref);
            flash.addFlashAttribute("success", "Booking cancelled");
        } catch (ResponseStatusException ex) {
            flash.addFlashAttribute("error",
                    ex.getReason() == null ? "Cancel failed" : ex.getReason());
            return "redirect:/book?lookupRef=" + ref.trim() + "#manage";
        }
        return "redirect:/book#manage";
    }
}