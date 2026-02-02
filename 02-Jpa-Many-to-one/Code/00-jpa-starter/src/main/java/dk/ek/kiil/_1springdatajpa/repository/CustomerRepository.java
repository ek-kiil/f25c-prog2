package dk.ek.kiil._1springdatajpa.repository;

import dk.ek.kiil._1springdatajpa.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Here we can make custom queries
    // We also get CRUD methods for free
}