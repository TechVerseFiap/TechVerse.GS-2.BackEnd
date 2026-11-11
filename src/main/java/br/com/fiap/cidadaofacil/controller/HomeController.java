package br.com.fiap.cidadaofacil.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

// Raiz redireciona para o Swagger; /api/status confirma que a API está no ar
@RestController
@Hidden
public class HomeController {
    @GetMapping("/")
    public ResponseEntity<Void> home() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/swagger-ui.html"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/api/status")
    public Map<String, Object> status() {
        return Map.of(
                "aplicacao", "Cidadão Fácil API",
                "status", "online",
                "horario", LocalDateTime.now().toString()
        );
    }
}
