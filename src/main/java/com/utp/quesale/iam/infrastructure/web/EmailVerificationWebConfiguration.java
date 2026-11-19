package com.utp.quesale.iam.infrastructure.web;

import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.iam.interfaces.web.EmailVerifiedInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EmailVerificationWebConfiguration implements WebMvcConfigurer {

    private final IamContextFacade iam;

    public EmailVerificationWebConfiguration(IamContextFacade iam) {
        this.iam = iam;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new EmailVerifiedInterceptor(iam))
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/email-verifications/**",
                        "/api/v1/universities/**",
                        "/api/v1/students/**",
                        "/api/v1/interests/**",
                        "/api/v1/skills/**");
    }
}
