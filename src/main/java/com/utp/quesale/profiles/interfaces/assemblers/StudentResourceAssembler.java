package com.utp.quesale.profiles.interfaces.assemblers;

import com.utp.quesale.profiles.application.commands.CreateStudentCommand;
import com.utp.quesale.profiles.application.dtos.ProfileDto;
import com.utp.quesale.profiles.application.dtos.StudentDto;
import com.utp.quesale.profiles.interfaces.resources.CreateStudentResource;
import com.utp.quesale.profiles.interfaces.resources.ProfileResource;
import com.utp.quesale.profiles.interfaces.resources.StudentResource;

public class StudentResourceAssembler {
    public static CreateStudentCommand toCommandFromResource(CreateStudentResource resource) {
        return new CreateStudentCommand(resource.universityId(), resource.email(),
                resource.firstName(), resource.lastName());
    }

    public static StudentResource toResourceFromDto(StudentDto dto) {
        ProfileDto profile = dto.profile();
        ProfileResource profileResource = profile == null ? null
                : new ProfileResource(profile.career(), profile.cycle(), profile.description(),
                        profile.visibility(), profile.interestIds(), profile.skillIds(),
                        profile.gender(), profile.age(), profile.campus(), profile.birthDate());
        return new StudentResource(dto.id(), dto.recordId(), dto.universityId(), dto.email(),
                dto.firstName(), dto.lastName(), dto.createdAt(), profileResource);
    }
}
