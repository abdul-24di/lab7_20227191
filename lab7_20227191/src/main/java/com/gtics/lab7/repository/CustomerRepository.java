package com.gtics.lab7.repository;

import com.gtics.lab7.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByDocument(String document);
    boolean existsByDocumentAndIdNot(String document, Integer id);
}