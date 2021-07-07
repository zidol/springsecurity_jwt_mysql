package co.kr.nakdong.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/index")
    public String index() {

        return "hello world";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/greeting")
    public String greeting() {
        return "hello";
    }
}
