package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.category.*;
import com.FermeDirecte.FermeDirecte.entity.Category;
import com.FermeDirecte.FermeDirecte.exception.BadRequestException;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable("categories")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getArbre() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryResponse creer(CategoryRequest request) {

        Category category = Category.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .build();

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie parente introuvable"));

            category.setParent(parent);
        }

        return toResponse(categoryRepository.save(category));
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryResponse modifier(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));

        category.setNom(request.getNom());
        category.setDescription(request.getDescription());

        if (request.getParentId() != null) {

            // ⚠️ éviter boucle (catégorie devient son propre parent)
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Une catégorie ne peut pas être son propre parent");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie parente introuvable"));

            category.setParent(parent);

        } else {
            category.setParent(null);
        }

        return toResponse(categoryRepository.save(category));
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public void supprimer(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));

        // ⚠️ empêcher suppression si تحتوي على sous-catégories
        if (!category.getSousCategories().isEmpty()) {
            throw new BadRequestException("Impossible de supprimer une catégorie avec des sous-catégories");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category c) {

        List<CategoryResponse> sousCategories = c.getSousCategories().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(c.getId())
                .nom(c.getNom())
                .description(c.getDescription())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .sousCategories(sousCategories)
                .build();
    }
}