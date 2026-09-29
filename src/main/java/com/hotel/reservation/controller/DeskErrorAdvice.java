package com.hotel.reservation.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
@ControllerAdvice(assignableTypes = {FinanceController.class, ReservationsController.class})
public class DeskErrorAdvice {
    @ExceptionHandler(ResponseStatusException.class)
    public String businessError(ResponseStatusException ex,
            HttpServletRequest request, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getReason() == null
                ? "Record not found" : ex.getReason());
        if (request.getRequestURI().startsWith(request.getContextPath() + "/reservations"))
            return "redirect:/reservations";
        return request.getRequestURI().contains("/finance")
                ? "redirect:/finance" : "redirect:/finance";
    }
}
