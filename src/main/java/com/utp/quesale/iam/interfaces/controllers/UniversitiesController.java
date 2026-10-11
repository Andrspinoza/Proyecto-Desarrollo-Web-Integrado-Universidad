package com.utp.quesale.iam.interfaces.controllers;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.exceptions.UniversityAlreadyExistsException;
import com.utp.quesale.iam.domain.exceptions.UniversityNotFoundException;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.domain.valueobjects.EmailDomain;
import com.utp.quesale.iam.interfaces.assemblers.UniversityResourceAssembler;
import com.utp.quesale.iam.interfaces.resources.CreateUniversityResource;
import com.utp.quesale.iam.interfaces.resources.UniversityResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// Sin capa de aplicación todavía: el controller usa directamente el repositorio del dominio
@RestController
@RequestMapping("/api/v1/universities")
@Tag(name = "Universities", description = "Universidades admitidas y su dominio de correo institucional")
public class UniversitiesController {
    private final UniversityRepository universityRepository;

    public UniversitiesController(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @PostMapping
    @Operation(summary = "Registrar una universidad")
    public ResponseEntity<UniversityResource> create(@Valid @RequestBody CreateUniversityResource resource) {
        EmailDomain emailDomain = new EmailDomain(resource.emailDomain());
        if (universityRepository.existsByEmailDomain(emailDomain.value()))
            throw new UniversityAlreadyExistsException(emailDomain.value());
        University saved = universityRepository.save(new University(resource.name(), emailDomain));
        return ResponseEntity.created(URI.create("/api/v1/universities/" + saved.getId()))
                .body(UniversityResourceAssembler.toResourceFromEntity(saved));
    }

    @GetMapping
    @Operation(summary = "Listar universidades")
    public List<UniversityResource> findAll() {
        return universityRepository.findAll().stream()
                .map(UniversityResourceAssembler::toResourceFromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una universidad")
    public UniversityResource findById(@PathVariable Long id) {
        return universityRepository.findById(id)
                .map(UniversityResourceAssembler::toResourceFromEntity)
                .orElseThrow(() -> new UniversityNotFoundException(id));
    }
}
