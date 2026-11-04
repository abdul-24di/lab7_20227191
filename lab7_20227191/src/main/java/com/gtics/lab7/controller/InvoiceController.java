package com.gtics.lab7.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gtics.lab7.dto.InvoiceForm;
import com.gtics.lab7.dto.LineForm;
import com.gtics.lab7.entity.Customer;
import com.gtics.lab7.entity.Invoice;
import com.gtics.lab7.entity.Product;
import com.gtics.lab7.repository.CustomerRepository;
import com.gtics.lab7.repository.InvoiceRepository;
import com.gtics.lab7.repository.ProductRepository;
import com.gtics.lab7.service.InvoiceService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceRepository invoiceRepository,
                             CustomerRepository customerRepository,
                             ProductRepository productRepository,
                             InvoiceService invoiceService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.invoiceService = invoiceService;
    }

    // Se ejecutan en cada request (también en el POST con errores), así los selects siempre tienen datos
    @ModelAttribute("customers")
    public List<Customer> customers() {
        return customerRepository.findAll();
    }

    @ModelAttribute("products")
    public List<Product> products() {
        return productRepository.findAll();
    }

    @ModelAttribute("types")
    public List<String> types() {
        return List.of("Factura", "Boleta");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("invoices", invoiceRepository.findAllByOrderByDateDescIdDesc());
        return "invoices/list";
    }

    @GetMapping("/detail")
    public String detalle(@RequestParam("id") Integer id, Model model, RedirectAttributes attr) {
        Invoice invoice = invoiceRepository.findWithDetailsById(id).orElse(null);
        if (invoice == null) {
            attr.addFlashAttribute("error", "El pedido no existe");
            return "redirect:/invoices";
        }
        model.addAttribute("invoice", invoice);
        return "invoices/detail";
    }

    @GetMapping("/new")
    public String nuevo(Model model) {
        InvoiceForm form = new InvoiceForm();
        form.setDate(LocalDate.now());
        completarFilas(form);
        model.addAttribute("invoiceForm", form);
        return "invoices/form";
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute("invoiceForm") @Valid InvoiceForm form,
                          BindingResult bindingResult,
                          RedirectAttributes attr) {

        invoiceService.validar(form, bindingResult);   // reglas adicionales sobre el mismo BindingResult

        if (bindingResult.hasErrors()) {
            completarFilas(form);
            return "invoices/form";   // conserva tipo, fecha, cliente y filas ingresadas
        }

        invoiceService.registrar(form);
        attr.addFlashAttribute("msg", "Pedido registrado correctamente");
        return "redirect:/invoices";
    }

    private void completarFilas(InvoiceForm form) {
        while (form.getLines().size() < InvoiceService.MAX_LINES) {
            form.getLines().add(new LineForm());
        }
    }
}