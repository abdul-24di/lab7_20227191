package com.gtics.lab7.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import com.gtics.lab7.dto.InvoiceForm;
import com.gtics.lab7.dto.LineForm;
import com.gtics.lab7.entity.Customer;
import com.gtics.lab7.entity.Invoice;
import com.gtics.lab7.entity.InvoiceDetail;
import com.gtics.lab7.entity.Product;
import com.gtics.lab7.repository.CustomerRepository;
import com.gtics.lab7.repository.InvoiceRepository;
import com.gtics.lab7.repository.ProductRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class InvoiceService {

    public static final int MAX_LINES = 5;

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CustomerRepository customerRepository,
                          ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    /** Reglas que van más allá de las anotaciones. Agrega errores al BindingResult. */
    public void validar(InvoiceForm form, BindingResult br) {

        Customer customer = null;
        if (form.getCustomerId() != null) {
            customer = customerRepository.findById(form.getCustomerId()).orElse(null);
            if (customer == null) {
                br.rejectValue("customerId", "customer.notfound", "El cliente seleccionado no existe");
            }
        }

        Set<Integer> productosUsados = new HashSet<>();
        int lineasUsadas = 0;

        for (int i = 0; i < form.getLines().size(); i++) {
            LineForm line = form.getLines().get(i);
            String campoProducto = "lines[" + i + "].productId";
            String campoCantidad = "lines[" + i + "].quantity";
            boolean errorBindingCantidad = br.hasFieldErrors(campoCantidad);   // ej. "abc"

            // Fila totalmente vacía: se ignora
            if (line.getProductId() == null && line.getQuantity() == null && !errorBindingCantidad) {
                continue;
            }
            lineasUsadas++;

            Product product = null;
            if (line.getProductId() == null) {
                br.rejectValue(campoProducto, "line.product.required", "Seleccione un producto");
            } else {
                product = productRepository.findById(line.getProductId()).orElse(null);
                if (product == null) {
                    br.rejectValue(campoProducto, "line.product.notfound", "El producto no existe");
                } else if (!productosUsados.add(product.getId())) {
                    br.rejectValue(campoProducto, "line.product.duplicate",
                            "El producto está repetido en el pedido");
                }
            }

            if (!errorBindingCantidad) {
                Integer qty = line.getQuantity();
                if (qty == null) {
                    br.rejectValue(campoCantidad, "line.quantity.required", "Ingrese la cantidad");
                } else if (qty <= 0) {
                    br.rejectValue(campoCantidad, "line.quantity.positive", "La cantidad debe ser mayor que cero");
                } else if (product != null && qty > product.getStock()) {
                    br.rejectValue(campoCantidad, "line.quantity.stock",
                            "La cantidad supera el stock disponible (" + product.getStock() + ")");
                }
            }
        }

        if (lineasUsadas == 0) {
            br.reject("lines.empty", "Debe incluir al menos un producto");
        }

        // Regla entre campos: tipo de comprobante vs documento del cliente (error global)
        if (customer != null && form.getType() != null && !br.hasFieldErrors("type")) {
            if ("Factura".equals(form.getType()) && !"RUC".equals(customer.getDocumentType())) {
                br.reject("type.mismatch", "Una Factura solo puede emitirse a un cliente con RUC");
            } else if ("Boleta".equals(form.getType()) && !"DNI".equals(customer.getDocumentType())) {
                br.reject("type.mismatch", "Una Boleta solo puede emitirse a un cliente con DNI");
            }
        }
    }

    /** Solo se llama cuando no hay errores. Subtotales y total se calculan en Java. */
    @Transactional
    public Invoice registrar(InvoiceForm form) {
        Customer customer = customerRepository.findById(form.getCustomerId()).orElseThrow();

        Invoice invoice = new Invoice();
        invoice.setType(form.getType());
        invoice.setDate(form.getDate());
        invoice.setCustomer(customer);

        for (LineForm line : form.getLines()) {
            if (line.getProductId() == null || line.getQuantity() == null) {
                continue;
            }
            Product product = productRepository.findById(line.getProductId()).orElseThrow();

            InvoiceDetail detail = new InvoiceDetail();
            detail.setProduct(product);
            detail.setQuantity(line.getQuantity());
            detail.setPrice(product.getPrice());                                  // precio al momento de la venta
            detail.setSubtotal(redondear(line.getQuantity() * product.getPrice())); // cantidad × precio
            invoice.addDetail(detail);
        }

        return invoiceRepository.save(invoice);   // guarda cabecera y detalles (cascade) en una transacción
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}