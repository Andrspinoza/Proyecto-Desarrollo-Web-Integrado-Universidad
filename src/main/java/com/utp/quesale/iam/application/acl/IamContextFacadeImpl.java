package com.utp.quesale.iam.application.acl;

import com.utp.quesale.iam.domain.aggregates.University;
import com.utp.quesale.iam.domain.repositories.UniversityRepository;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

// Implementación de la fachada: profiles la usa para validar la universidad y el correo de un estudiante
@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UniversityRepository universityRepository;

    public IamContextFacadeImpl(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @Override
    public boolean isActiveUniversity(Long universityId) {
        if (universityId == null || universityId <= 0) {
            return false;
        }
        return universityRepository.findById(universityId).map(University::isActive).orElse(false);
    }

    @Override
    public boolean universityAcceptsEmail(Long universityId, String email) {
        if (universityId == null || universityId <= 0) {
            return false;
        }
        return universityRepository.findById(universityId)
                .map(university -> university.accepts(email))
                .orElse(false);
    }
}
