package com.gtics.lab7.repository;



import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gtics.lab7.entity.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    @EntityGraph(attributePaths = {"customer", "details"})
    List<Invoice> findAllByOrderByDateDescIdDesc();

    @EntityGraph(attributePaths = {"customer", "details", "details.product"})
    Optional<Invoice> findWithDetailsById(Integer id);
}