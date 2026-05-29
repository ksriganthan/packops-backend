package ch.packops.packopsbackend.controller;

import ch.packops.packopsbackend.dto.CategoryCreationDto;
import ch.packops.packopsbackend.dto.CategoryDto;

import ch.packops.packopsbackend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Teodor Glisic
 */

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }


    @GetMapping("/by")
    public ResponseEntity<List<CategoryDto>> getAllCategories(@RequestParam(name= "language" ,required = true, defaultValue = "en") String language) {
        return ResponseEntity.ok(categoryService.getCategoryNamesByLanguage(language));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createNewCategory(@RequestBody CategoryCreationDto categoryCreationDto) {
        return ResponseEntity.ok().body(categoryService.createNewCategory(categoryCreationDto));
    }

}
