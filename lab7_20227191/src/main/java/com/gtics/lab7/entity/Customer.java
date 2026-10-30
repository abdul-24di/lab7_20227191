package com.gtics.lab7.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customer")
public class Customer {

    // id AUTO_INCREMENT en la BD -> IDENTITY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    // Hibernate lo mapea solo a la columna document_type
    @NotBlank(message = "Seleccione el tipo de documento")
    @Pattern(regexp = "^(DNI|RUC)?$", message = "El tipo de documento solo puede ser DNI o RUC")
    @Column(name = "document_type")
    private String documentType;

    // La regla "8 digitos si DNI / 11 si RUC" depende de OTRO campo,
    // por eso se valida en el controlador (ver CustomerController)
    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "^\\d*$", message = "El documento solo debe contener dígitos")
    private String document;

    public Customer() {
    }

  //get an dset

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
}