package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Brand;
import org.fsm.service.BrandService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/brand")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // List all brands (dùng khi load /staff)
    @GetMapping
    public String getAllBrands(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
        return "staff";
    }

    // AJAX: Get single brand by ID → /staff/brand/1
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Brand> getBrandById(@PathVariable Integer id) {
        return brandService.getBrandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    public String saveBrand(@ModelAttribute Brand brand, RedirectAttributes ra) {
        try {
            if (brand.getId() != null) {
                brandService.updateBrand(brand.getId(), brand);
                ra.addFlashAttribute("successMessage", "Brand updated successfully!");
            } else {
                brandService.createBrand(brand);
                ra.addFlashAttribute("successMessage", "Brand created successfully!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/staff#brands";
    }

    @PostMapping("/{id}/delete")
    public String deleteBrand(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            brandService.deleteBrand(id);
            ra.addFlashAttribute("successMessage", "Brand deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error deleting brand: " + e.getMessage());
        }
        return "redirect:/staff#brands";
    }
}
