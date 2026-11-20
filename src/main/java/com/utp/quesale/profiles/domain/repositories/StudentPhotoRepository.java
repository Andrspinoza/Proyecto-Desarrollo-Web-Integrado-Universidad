package com.utp.quesale.profiles.domain.repositories;

import com.utp.quesale.profiles.domain.valueobjects.StudentPhoto;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface StudentPhotoRepository {
    void replace(StudentPhoto photo);

    Optional<StudentPhoto> findByKey(String key);

    Map<Long, String> findKeysByStudentIds(Collection<Long> studentIds);

    void deleteByStudentId(Long studentId);
}
