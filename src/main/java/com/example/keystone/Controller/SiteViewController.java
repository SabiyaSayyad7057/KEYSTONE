package com.example.keystone.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.keystone.Entity.Site;
import com.example.keystone.Service.SiteService;

@Controller
@RequestMapping("/sites")
public class SiteViewController {

    private final SiteService siteService;

    public SiteViewController(SiteService siteService) {
        this.siteService = siteService;
    }

    // ---------------------------------
    // Site List
    // ---------------------------------

    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "sites",
                siteService.getAllSites()
        );

        return "sites/list";
    }

    // ---------------------------------
    // Add Site Form
    // ---------------------------------

    @GetMapping("/new")
    public String createForm(Model model) {

        model.addAttribute(
                "site",
                new Site()
        );

        model.addAttribute(
                "customers",
                siteService.getAllCustomers()
        );

        return "sites/form";
    }

    // ---------------------------------
    // Save Site
    // ---------------------------------

    @PostMapping
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam Long customerId) {

        siteService.saveSite(
                id,
                name,
                address,
                customerId
        );

        return "redirect:/sites";
    }

    // ---------------------------------
    // Edit Site
    // ---------------------------------

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "site",
                siteService.getSiteById(id)
        );

        model.addAttribute(
                "customers",
                siteService.getAllCustomers()
        );

        return "sites/form";
    }

    // ---------------------------------
    // Delete Site
    // ---------------------------------

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            siteService.deleteSite(id);
            redirectAttributes.addFlashAttribute("success", "Site deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", "Site cannot be deleted because it is linked to a work order.");
        }
        return "redirect:/sites";
    }
}