package com.utp.quesale.profiles.interfaces.controllers;

import com.utp.quesale.profiles.application.dtos.RecommendationDto;
import com.utp.quesale.profiles.application.handlers.queries.GetStudentPhotoQueryHandler;
import com.utp.quesale.profiles.application.handlers.queries.RecommendStudentsQueryHandler;
import com.utp.quesale.profiles.domain.valueobjects.Gender;
import com.utp.quesale.shared.interfaces.rest.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students/recommendations")
@Tag(name = "Recommendations", description = "Estudiantes sugeridos con su afinidad")
public class StudentRecommendationsController {
    private final RecommendStudentsQueryHandler handler;
    private final GetStudentPhotoQueryHandler photos;

    public StudentRecommendationsController(RecommendStudentsQueryHandler handler, GetStudentPhotoQueryHandler photos) {
        this.handler = handler;
        this.photos = photos;
    }

    public record RecommendationResource(Long id, String firstName, String lastName, Integer age, Gender gender,
                                         String career, int cycle, String campus, String description,
                                         int affinityPercent, List<String> sharedInterests, List<String> sharedSkills,
                                         boolean sameCareer, boolean sameCampus, int cycleDifference,
                                         List<String> interests, String photoUrl) {}

    @GetMapping
    @Operation(summary = "Estudiantes de mi universidad con quienes más afinidad tengo",
            description = "La afinidad (0 a 100) ordena las sugerencias: 8 puntos por interés en común (hasta 5), 2 por habilidad en común (hasta 5), 25 por la misma carrera, hasta 15 por ciclo cercano y 10 por la misma sede. "
                    + "No es un match: solo una solicitud de conexión aceptada lo es. Excluye a quien ya está conectado, tiene solicitud pendiente, está bloqueado, no verificó su correo o tiene el perfil privado.")
    public List<RecommendationResource> recommendations(@RequestHeader(RequestHeaders.STUDENT_ID) Long viewerId,
                                                        @RequestParam(defaultValue = "20") int limit) {
        List<RecommendationDto> result = handler.handle(viewerId, limit);
        Map<Long, String> keys = photos.keysOf(result.stream().map(RecommendationDto::id).toList());
        return result.stream().map(item -> new RecommendationResource(item.id(), item.firstName(), item.lastName(),
                item.age(), item.gender(), item.career(), item.cycle(), item.campus(), item.description(),
                item.affinityPercent(), item.sharedInterests(), item.sharedSkills(), item.sameCareer(),
                item.sameCampus(), item.cycleDifference(), item.interests(),
                keys.containsKey(item.id()) ? StudentPhotosController.PHOTO_PATH + keys.get(item.id()) : null)).toList();
    }
}
