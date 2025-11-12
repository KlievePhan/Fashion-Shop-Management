package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Brand;
import org.fsm.service.BrandService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // Get all brands (for admin page)
    @GetMapping
    public String getAllBrands(Model model) {
        List<Brand> brands = brandService.getAllBrands();
        model.addAttribute("brands", brands);
        return "admin"; // Returns admin.html view
    }

    // Get single brand by ID (for AJAX edit)
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Brand> getBrandById(@PathVariable Integer id) {
        return brandService.getBrandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create or Update brand
    @PostMapping("/save")
    public String saveBrand(@ModelAttribute Brand brand, RedirectAttributes redirectAttributes) {
        try {
            if (brand.getId() != null) {
                // Update existing brand
                Brand updated = brandService.updateBrand(brand.getId(), brand);
                redirectAttributes.addFlashAttribute("successMessage", "Brand updated successfully!");
            } else {
                // Create new brand
                Brand created = brandService.createBrand(brand);
                redirectAttributes.addFlashAttribute("successMessage", "Brand created successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin#brands";
    }

    // Delete brand
    @PostMapping("/{id}/delete")
    public String deleteBrand(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("successMessage", "Brand deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting brand: " + e.getMessage());
        }
        return "redirect:/admin#brands";
    }
}