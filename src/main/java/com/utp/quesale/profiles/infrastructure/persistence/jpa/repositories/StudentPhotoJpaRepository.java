package com.utp.quesale.profiles.infrastructure.persistence.jpa.repositories;

import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentPhotoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentPhotoJpaRepository extends JpaRepository<StudentPhotoJpaEntity, Long> {
    interface KeyView {
        Long getStudentId();

        String getPhotoKey();
    }

    Optional<StudentPhotoJpaEntity> findByPhotoKey(String photoKey);

    List<KeyView> findByStudentIdIn(Collection<Long> studentIds);

    @Modifying
    @Query("delete from StudentPhotoJpaEntity p where p.studentId = :studentId")
    void deleteByStudentId(Long studentId);
}
