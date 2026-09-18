package com.hotel.reservation.controller;
import com.hotel.reservation.entity.*;
import com.hotel.reservation.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
@Controller
@RequestMapping("/inventory")
public class InventoryController {
 private final InventoryService service;
 public InventoryController(InventoryService s){service=s;}
 @GetMapping public String dashboard(Model m){m.addAttribute("items",service.list());return "inventory/dashboard";}
 @GetMapping("/items/new") public String create(Model m){m.addAttribute("item",new InventoryItem());return "inventory/item-form";}
 @GetMapping("/items/{id}/edit") public String edit(@PathVariable Long id,Model m){m.addAttribute("item",service.get(id));return "inventory/item-form";}
 @PostMapping("/items/save") public String save(@RequestParam(required=false) Long id,@RequestParam String code,@RequestParam String name,@RequestParam String category,@RequestParam String unit,@RequestParam String location,@RequestParam long minimumStock,RedirectAttributes f){service.save(id,code,name,category,unit,location,minimumStock);f.addFlashAttribute("success","Item saved");return "redirect:/inventory";}
 @PostMapping("/items/{id}/delete") public String delete(@PathVariable Long id){service.delete(id);return "redirect:/inventory";}
 @PostMapping("/items/{id}/archive") public String archive(@PathVariable Long id){service.archive(id);return "redirect:/inventory";}
 @GetMapping("/items/{id}/movement") public String movement(@PathVariable Long id,Model m){m.addAttribute("item",service.get(id));m.addAttribute("requestKey",UUID.randomUUID().toString());return "inventory/stock-movement-form";}
 @PostMapping("/items/{id}/movement") public String move(@PathVariable Long id,@RequestParam StockMovement.Type type,@RequestParam long quantity,@RequestParam String department,@RequestParam(defaultValue="") String note,@RequestParam String requestKey,RedirectAttributes f){service.move(id,type,quantity,department,note,requestKey);f.addFlashAttribute("success","Stock movement recorded");return "redirect:/inventory";}
 @GetMapping("/history") public String history(@RequestParam(required=false) Long itemId,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("history",service.history(itemId,page));m.addAttribute("itemId",itemId);return "inventory/history";}
 @ExceptionHandler(ResponseStatusException.class) public String error(ResponseStatusException e,RedirectAttributes f){f.addFlashAttribute("error",e.getReason());return "redirect:/inventory";}
 @ExceptionHandler(DataIntegrityViolationException.class) public String duplicate(RedirectAttributes f){f.addFlashAttribute("error","Conflicting item code or request key. Check history before retrying.");return "redirect:/inventory";}
}
