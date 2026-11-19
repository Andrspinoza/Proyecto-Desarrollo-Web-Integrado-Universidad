package com.utp.quesale.iam.infrastructure.security;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auth_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_auth_student", columnNames = "student_id")})
public class AccountEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String email;
    @Column(name = "password_hash", nullable = false, length = 100) private String passwordHash;
    @Column(name = "student_id", nullable = false) private Long studentId;
    protected AccountEntity() {}
    public AccountEntity(String email, String passwordHash, Long studentId) {
        this.email = email; this.passwordHash = passwordHash; this.studentId = studentId;
    }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public Long getStudentId() { return studentId; }
}
