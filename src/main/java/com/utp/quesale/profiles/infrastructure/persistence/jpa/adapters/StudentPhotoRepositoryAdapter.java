package com.utp.quesale.profiles.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.profiles.domain.repositories.StudentPhotoRepository;
import com.utp.quesale.profiles.domain.valueobjects.StudentPhoto;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentPhotoJpaEntity;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.StudentJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories.StudentPhotoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class StudentPhotoRepositoryAdapter implements StudentPhotoRepository {
    private final StudentPhotoJpaRepository photos;
    private final StudentJpaRepository students;
    private final Clock clock;

    public StudentPhotoRepositoryAdapter(StudentPhotoJpaRepository photos, StudentJpaRepository students, Clock clock) {
        this.photos = photos;
        this.students = students;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void replace(StudentPhoto photo) {
        var student = students.findById(photo.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + photo.studentId()));
        StudentPhotoJpaEntity entity = photos.findById(photo.studentId()).orElseGet(StudentPhotoJpaEntity::new);
        entity.setStudent(student);
        entity.setPhotoKey(photo.key());
        entity.setContentType(photo.contentType());
        entity.setData(photo.data());
        entity.setUpdatedAt(clock.instant());
        photos.save(entity);
    }

    @Override
    public Optional<StudentPhoto> findByKey(String key) {
        return photos.findByPhotoKey(key)
                .map(entity -> new StudentPhoto(entity.getStudentId(), entity.getPhotoKey(),
                        entity.getContentType(), entity.getData()));
    }

    @Override
    public Map<Long, String> findKeysByStudentIds(Collection<Long> studentIds) {
        Map<Long, String> keys = new HashMap<>();
        if (studentIds == null || studentIds.isEmpty()) return keys;
        photos.findByStudentIdIn(studentIds).forEach(view -> keys.put(view.getStudentId(), view.getPhotoKey()));
        return keys;
    }

    @Override
    @Transactional
    public void deleteByStudentId(Long studentId) {
        photos.deleteByStudentId(studentId);
    }
}
