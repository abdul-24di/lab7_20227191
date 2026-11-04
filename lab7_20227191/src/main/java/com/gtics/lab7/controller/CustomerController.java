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

    @PostMapping("/save")
    public String guardar(@ModelAttribute("customer") @Valid Customer customer,
                          BindingResult bindingResult,
                          RedirectAttributes attr) {

        validarDocumento(customer, bindingResult);

        if (bindingResult.hasErrors()) {
            return "customers/form";   // sin redirect: se conserva el objeto y los errores
        }

        boolean esNuevo = customer.getId() == null;
        customerRepository.save(customer);
        attr.addFlashAttribute("msg", esNuevo ? "Cliente registrado" : "Cliente actualizado");
        return "redirect:/customers";
    }

    @PostMapping("/delete")
    public String eliminar(@RequestParam("id") Integer id, RedirectAttributes attr) {
        try {
            customerRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Cliente eliminado");
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("error", "No se puede eliminar: el cliente tiene pedidos asociados");
        }
        return "redirect:/customers";
    }

    // Reglas que dependen de más de un campo y unicidad
    private void validarDocumento(Customer customer, BindingResult br) {
        if (br.hasFieldErrors("documentType") || br.hasFieldErrors("document")) {
            return;
        }

        int longitud = "DNI".equals(customer.getDocumentType()) ? 8 : 11;
        if (customer.getDocument().length() != longitud) {
            br.rejectValue("document", "document.length",
                    customer.getDocumentType() + " debe contener exactamente " + longitud + " dígitos");
            return;
        }

        boolean duplicado = (customer.getId() == null)
                ? customerRepository.existsByDocument(customer.getDocument())
                : customerRepository.existsByDocumentAndIdNot(customer.getDocument(), customer.getId());
        if (duplicado) {
            br.rejectValue("document", "document.duplicate",
                    "Ya existe un cliente registrado con ese número de documento");
        }
    }
}