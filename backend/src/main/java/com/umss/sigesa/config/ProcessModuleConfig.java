package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.CreateAccreditationProcessUseCase;
import com.umss.sigesa.application.port.out.AccreditationProcessRepositoryPort;
import com.umss.sigesa.application.port.out.TemplateRepositoryPort;
import com.umss.sigesa.application.service.CreateAccreditationProcessService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessModuleConfig {

    @Bean
    CreateAccreditationProcessUseCase createAccreditationProcessUseCase(
            AccreditationProcessRepositoryPort processRepository,
            TemplateRepositoryPort templateRepository) {
        return new CreateAccreditationProcessService(processRepository, templateRepository);
    }
}
