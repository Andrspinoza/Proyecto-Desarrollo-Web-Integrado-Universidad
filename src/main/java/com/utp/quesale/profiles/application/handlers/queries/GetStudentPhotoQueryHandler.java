package com.utp.quesale.profiles.application.handlers.queries;

import com.utp.quesale.profiles.application.exceptions.PhotoNotFoundException;
import com.utp.quesale.profiles.domain.repositories.StudentPhotoRepository;
import com.utp.quesale.profiles.domain.valueobjects.StudentPhoto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetStudentPhotoQueryHandler {
    private final StudentPhotoRepository photos;

    public GetStudentPhotoQueryHandler(StudentPhotoRepository photos) {
        this.photos = photos;
    }

    public StudentPhoto byKey(String key) {
        if (key == null || key.isBlank()) throw new PhotoNotFoundException();
        return photos.findByKey(key).orElseThrow(PhotoNotFoundException::new);
    }

    public Optional<String> keyOf(Long studentId) {
        return Optional.ofNullable(photos.findKeysByStudentIds(java.util.List.of(studentId)).get(studentId));
    }

    public Map<Long, String> keysOf(Collection<Long> studentIds) {
        return photos.findKeysByStudentIds(studentIds);
    }
}
