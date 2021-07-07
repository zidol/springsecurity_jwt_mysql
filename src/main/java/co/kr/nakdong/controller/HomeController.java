package co.kr.nakdong.controller;

import co.kr.nakdong.entity.User;
import co.kr.nakdong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/index")
    public String index() {

        return "hello world";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/greeting")
    public String greeting() {
        return "hello";
    }


    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/user/{email}")
    public ResponseEntity<User> test(@PathVariable String email) {
        User user = userService.findByEmail(email).get();
        return ResponseEntity.ok(user);
    }
}
