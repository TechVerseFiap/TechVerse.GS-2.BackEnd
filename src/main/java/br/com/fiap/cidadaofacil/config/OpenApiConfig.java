package br.com.fiap.cidadaofacil.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Informações exibidas no Swagger
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI cidadaoFacilOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Cidadão Fácil API")
                .version("1.0.0")
                .description("CitizenTech - Global Solution FIAP 2026.2. Catálogo de serviços públicos e assistente "
                        + "que orienta o cidadão até a fonte oficial. Dados demonstrativos: confirme sempre "
                        + "no canal oficial do órgão responsável."));
    }
}
