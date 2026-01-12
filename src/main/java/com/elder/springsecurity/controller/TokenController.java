package com.elder.springsecurity.controller;

import com.elder.springsecurity.dto.LoginRequestDTO;
import com.elder.springsecurity.dto.LoginResponseDTO;
import com.elder.springsecurity.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class TokenController {

    private final JwtEncoder jwtEncoder; // Responsável por gerar (assinar) o JWT, Usa a chave privada RSA

    private final UserRepository repository;

    private BCryptPasswordEncoder bCryptPasswordEncoder; // Comparar a senha digitada com a senha do banco

    public TokenController(JwtEncoder jwtEncoder, UserRepository repository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.repository = repository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {

        var user = repository.findByUsername(loginRequest.username());

        if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("User or password is invalid!");
        }

        var now = Instant.now();
        var expiresIn = 300L; // token válido por 5 minutos

        // Extração das roles do usuário: ROLE_ADMIN ou ROLE_BASIC
        var roles = user.get().getRoles().stream().map(
                role -> role.getRoleName().name()
        ).toList();

        // Criação das claims do JWT, montando o conteúdo do token.
        var claims = JwtClaimsSet.builder()
                .issuer("mybackend") // iss. Quem emitiu o token
                .subject(user.get().getUserId().toString()) // sub. Identidade do usuário, geralmente ID do usuario ou username unico.
                .issuedAt(now) // iat, Quando foi criado
                .expiresAt(now.plusSeconds(expiresIn)) // exp, quando o token expira.
                .claim("scope", roles)
                .build();

        // Geração do JWT, claims são assinadas com a chave privada, Gera um JWT no formato: HEADER.PAYLOAD.SIGNATURE
        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponseDTO(jwtValue, expiresIn));
    }
}
