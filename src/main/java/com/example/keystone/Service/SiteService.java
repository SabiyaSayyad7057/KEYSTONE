  package com.example.keystone.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.keystone.Entity.Customer;
import com.example.keystone.Entity.Site;
import com.example.keystone.Repo.CustomerRepository;
import com.example.keystone.Repo.SiteRepository;

@Service
public class SiteService {

    private final SiteRepository siteRepository;

    private final CustomerRepository customerRepository;

    public SiteService(
            SiteRepository siteRepository,
            CustomerRepository customerRepository) {

        this.siteRepository = siteRepository;
        this.customerRepository = customerRepository;
    }

    // ---------------------------------
    // Get all sites
    // ---------------------------------

    public List<Site> getAllSites() {

        return siteRepository.findAll();
    }

    // ---------------------------------
    // Get site by ID
    // ---------------------------------

    public Site getSiteById(Long id) {

        return siteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Site not found with ID: " + id
                        )
                );
    }

    // ---------------------------------
    // Get all customers
    // ---------------------------------

    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    // ---------------------------------
    // Save Site
    // ---------------------------------

    public Site saveSite(
            Long id,
            String name,
            String address,
            Long customerId) {

        Site site;

        // New site
        if (id == null) {

            site = new Site();

        } else {

            // Existing site
            site = getSiteById(id);
        }

        // Find customer
        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found with ID: "
                                                + customerId
                                )
                        );

        // Set site data
        site.setName(name);
        site.setAddress(address);
        site.setCustomer(customer);

        // Save
        return siteRepository.save(site);
    }

    // ---------------------------------
    // Delete Site
    // ---------------------------------

    public void deleteSite(Long id) {

        if (!siteRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Site not found with ID: " + id
            );
        }

        siteRepository.deleteById(id);
    }
}