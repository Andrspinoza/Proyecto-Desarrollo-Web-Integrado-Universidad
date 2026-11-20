package com.utp.quesale.profiles.interfaces;

import com.utp.quesale.profiles.domain.repositories.StudentPhotoRepository;
import com.utp.quesale.profiles.domain.valueobjects.StudentPhoto;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryPhotos implements StudentPhotoRepository {
    private final Map<Long, StudentPhoto> byStudent = new LinkedHashMap<>();

    @Override
    public void replace(StudentPhoto photo) {
        byStudent.put(photo.studentId(), photo);
    }

    @Override
    public Optional<StudentPhoto> findByKey(String key) {
        return byStudent.values().stream().filter(photo -> photo.key().equals(key)).findFirst();
    }

    @Override
    public Map<Long, String> findKeysByStudentIds(Collection<Long> studentIds) {
        Map<Long, String> keys = new HashMap<>();
        studentIds.forEach(id -> {
            if (byStudent.containsKey(id)) keys.put(id, byStudent.get(id).key());
        });
        return keys;
    }

    @Override
    public void deleteByStudentId(Long studentId) {
        byStudent.remove(studentId);
    }
}
