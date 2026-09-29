package com.hotel.reservation.controller;
import com.hotel.reservation.service.DeskOperations;
import com.hotel.reservation.entity.DeskStay;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;
@Controller
@RequestMapping("/finance")
public class FinanceController {
    private final DeskOperations desk;
    public FinanceController(DeskOperations desk) { this.desk = desk; }
    @GetMapping
    public String dashboard(Model model) {
        var stays = desk.stays();
        model.addAttribute("stays", stays);
        model.addAttribute("payments", desk.payments());
        model.addAttribute("reservations", desk.reservations());
        model.addAttribute("requestKey", UUID.randomUUID().toString());
        model.addAttribute("billed", stays.stream().map(DeskStay::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("collected", stays.stream().map(DeskStay::getPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("outstanding", stays.stream().map(DeskStay::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "finance/dashboard";
    }
    @PostMapping("/payments")
    public String pay(@RequestParam Long stayId, @RequestParam BigDecimal amount,
                      @RequestParam String requestKey) {
        desk.pay(stayId, amount, requestKey);
        return "redirect:/finance";
    }
    @PostMapping("/checkin")
    public String checkin(@RequestParam Long reservationId) {
        desk.checkInReservation(reservationId);
        return "redirect:/finance";
    }
}
