package com.gtics.lab7.controller;


import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gtics.lab7.entity.Product;
import com.gtics.lab7.repository.ProductRepository;

/*
 * se implemento messages.properties por el error de conversion.
 * (claves typeMismatch.product.price y typeMismatch.product.stock). No se usa @InitBinder.
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products/list";
    }

    @GetMapping("/new")
    public String nuevo(Model model) {
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @GetMapping("/edit")
    public String editar(@RequestParam("id") Integer id, Model model, RedirectAttributes attr) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            attr.addFlashAttribute("error", "El producto no existe");
            return "redirect:/products";
        }
        model.addAttribute("product", product);   // mismo nombre que th:object="${product}"
        return "products/form";
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute("product") @Valid Product product,
                          BindingResult bindingResult,
                          RedirectAttributes attr) {

        // Nombre duplicado (solo si el nombre es válido)
        if (!bindingResult.hasFieldErrors("name")) {
            boolean duplicado = (product.getId() == null)
                    ? productRepository.existsByName(product.getName())
                    : productRepository.existsByNameAndIdNot(product.getName(), product.getId());
            if (duplicado) {
                bindingResult.rejectValue("name", "name.duplicate",
                        "Ya existe un producto con ese nombre");
            }
        }

        //incluye errores de validación Y de conversión (typeMismatch en price / stock)
        if (bindingResult.hasErrors()) {
            return "products/form";
        }

        boolean esNuevo = product.getId() == null;
        productRepository.save(product);
        attr.addFlashAttribute("msg", esNuevo ? "Producto registrado" : "Producto actualizado");
        return "redirect:/products";
    }

    @PostMapping("/delete")
    public String eliminar(@RequestParam("id") Integer id, RedirectAttributes attr) {
        try {
            productRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Producto eliminado");
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("error", "No se puede eliminar: el producto está en pedidos registrados");
        }
        return "redirect:/products";
    }
}