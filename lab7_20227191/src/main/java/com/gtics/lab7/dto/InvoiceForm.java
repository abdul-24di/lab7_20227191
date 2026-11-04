package com.gtics.lab7.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceForm {

    @NotBlank(message = "Seleccione el tipo de comprobante")
    @Pattern(regexp = "^(Factura|Boleta)?$", message = "El tipo debe ser Factura o Boleta")
    private String type;

    @NotNull(message = "Ingrese la fecha")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotNull(message = "Seleccione un cliente")
    private Integer customerId;

    private List<LineForm> lines = new ArrayList<>();

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public List<LineForm> getLines() { return lines; }
    public void setLines(List<LineForm> lines) { this.lines = lines; }
}