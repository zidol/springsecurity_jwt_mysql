package co.kr.nakdong.controller;

import co.kr.nakdong.entity.board.Board;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BoardController {

    @GetMapping("/posts")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Board> getPosts() {

        return null;
    }
}
