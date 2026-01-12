package com.elder.springsecurity.controller;

import com.elder.springsecurity.dto.CreateTweetDTO;
import com.elder.springsecurity.dto.FeedDTO;
import com.elder.springsecurity.dto.FeedItemDTO;
import com.elder.springsecurity.entities.Tweet;
import com.elder.springsecurity.entities.enums.RoleName;
import com.elder.springsecurity.repository.TweetRepository;
import com.elder.springsecurity.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
public class TweetController {

    private final TweetRepository tweetRepository;

    private final UserRepository userRepository;

    public TweetController(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/tweets")
    public ResponseEntity<Void> create(@RequestBody CreateTweetDTO dto,
                                                    JwtAuthenticationToken token //O Spring injeta automaticamente o token JWT já validado.
    ) {

        var user = userRepository.findById(UUID.fromString(token.getName())); // Buscando usuario Logado, Converte o sub para UUID

        // Associa o tweet ao usuário autenticado, Evita spoofing (ninguém cria tweet em nome de outro):
        Tweet tweet = new Tweet();
        tweet.setUser(user.get());
        tweet.setContent(dto.content());

        tweetRepository.save(tweet);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, JwtAuthenticationToken token) {

        UUID userIdFromToken = UUID.fromString(token.getName()); // Usuario autenticado pelo token

        var user = userRepository.findById(userIdFromToken).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Tweet tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Percorre as roles do usuário, Verifica se tem ROLE_ADMIN
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);

        // Verifica se é ADMIN ou dono do TWEET para deleção
        if (isAdmin || tweet.getUser().getUserId().equals(userIdFromToken)) {
            tweetRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDTO> findAll(@RequestParam(value = "page", defaultValue = "0") int page, // Parâmetros de paginação
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        // Consulta paginada e ordenada
        var tweets = tweetRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(tweet ->
                        new FeedItemDTO(
                                tweet.getTweetId(),
                                tweet.getContent(),
                                tweet.getUser().getUsername())
                );

        FeedDTO obj = new FeedDTO(tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements());
        return ResponseEntity.ok().body(obj);
    }

}
