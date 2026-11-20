package com.utp.quesale.profiles.interfaces.controllers;

import com.utp.quesale.profiles.application.handlers.commands.StudentPhotoCommandHandler;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentPhotoQueryHandler;
import com.utp.quesale.profiles.domain.valueobjects.StudentPhoto;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@RestController
@Tag(name = "Photos", description = "Foto de perfil")
public class StudentPhotosController {
    public static final String PHOTO_PATH = "/api/v1/photos/";

    private final StudentPhotoCommandHandler commands;
    private final GetStudentPhotoQueryHandler queries;

    public StudentPhotosController(StudentPhotoCommandHandler commands, GetStudentPhotoQueryHandler queries) {
        this.commands = commands;
        this.queries = queries;
    }

    public record PhotoResource(String photoUrl) {}

    @PutMapping(value = "/api/v1/students/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir o reemplazar mi foto de perfil",
            description = "Imagen JPEG, PNG o WebP de hasta 1 MB, en el campo `file`. El tipo se verifica por el contenido, no por el nombre.")
    public PhotoResource upload(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId,
                                @RequestParam("file") MultipartFile file) {
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Photo could not be read");
        }
        return new PhotoResource(PHOTO_PATH + commands.save(studentId, data));
    }

    @DeleteMapping("/api/v1/students/me/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Quitar mi foto de perfil")
    public void delete(@RequestHeader(RequestHeaders.STUDENT_ID) Long studentId) {
        commands.delete(studentId);
    }

    @GetMapping("/api/v1/photos/{key}")
    @Operation(summary = "Descargar una foto por su clave",
            description = "Pública: la clave es aleatoria y solo se entrega en los perfiles visibles. Cambia con cada foto nueva.")
    public ResponseEntity<byte[]> download(@PathVariable String key) {
        StudentPhoto photo = queries.byKey(key);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(photo.data());
    }
}
