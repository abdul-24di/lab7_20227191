package com.gtics.lab7.controller;


import com.gtics.lab7.entity.Customer;
import com.gtics.lab7.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gtics.lab7.repository.CustomerRepository;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @ModelAttribute("documentTypes")
    public List<String> documentTypes() {
        return List.of("DNI", "RUC");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "customers/list";
    }

    @GetMapping("/new")
    public String nuevo(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @GetMapping("/edit")
    public String editar(@RequestParam("id") Integer id, Model model, RedirectAttributes attr) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            attr.addFlashAttribute("error", "El cliente no existe");
            return "redirect:/customers";
        }
        model.addAttribute("customer", customer);   // mismo nombre que th:object="${customer}"
        return "customers/form";
    }
}