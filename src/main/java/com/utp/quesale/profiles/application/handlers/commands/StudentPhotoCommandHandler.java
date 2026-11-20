package com.utp.quesale.profiles.application.handlers.commands;

import com.utp.quesale.profiles.application.exceptions.StudentNotFoundException;
import com.utp.quesale.profiles.domain.repositories.StudentPhotoRepository;
import com.utp.quesale.profiles.domain.repositories.StudentRepository;
import com.utp.quesale.profiles.domain.valueobjects.StudentPhoto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentPhotoCommandHandler {
    private final StudentPhotoRepository photos;
    private final StudentRepository students;

    public StudentPhotoCommandHandler(StudentPhotoRepository photos, StudentRepository students) {
        this.photos = photos;
        this.students = students;
    }

    @Transactional
    public String save(Long studentId, byte[] data) {
        if (studentId == null || studentId <= 0) throw new IllegalArgumentException("Student id must be positive");
        if (!students.existsById(studentId)) throw new StudentNotFoundException(studentId);
        StudentPhoto photo = StudentPhoto.create(studentId, data);
        photos.replace(photo);
        return photo.key();
    }

    @Transactional
    public void delete(Long studentId) {
        if (studentId == null || studentId <= 0) throw new IllegalArgumentException("Student id must be positive");
        photos.deleteByStudentId(studentId);
    }
}
