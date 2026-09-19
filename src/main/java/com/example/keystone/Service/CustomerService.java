package com.example.keystone.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.keystone.Entity.Customer;
import com.example.keystone.Repo.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // Get customer by ID
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found with ID: " + id
                        )
                );
    }

    // Create or update customer
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // Delete customer
    public void deleteCustomer(Long id) {

        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Customer not found with ID: " + id
            );
        }

        customerRepository.deleteById(id);
    }
}