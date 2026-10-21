package com.utp.quesale.iam.interfaces.acl;

// Lo que otros contextos pueden preguntarle a iam. Las operaciones de cuenta (existsAccount, isAdmin…) se
// agregan cuando exista Account.
public interface IamContextFacade {

    // La universidad existe y está activa
    boolean isActiveUniversity(Long universityId);

    // El correo pertenece al dominio institucional de esa universidad (false si la universidad no existe)
    boolean universityAcceptsEmail(Long universityId, String email);
}
