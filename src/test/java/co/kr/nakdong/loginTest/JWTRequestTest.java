package co.kr.nakdong.loginTest;


import co.kr.nakdong.dto.UserLoginDto;
import co.kr.nakdong.entity.author.User;
import co.kr.nakdong.entity.board.Board;
import co.kr.nakdong.repository.BoardRepository;
import co.kr.nakdong.repository.UserRepository;
import co.kr.nakdong.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JWTRequestTest extends WebIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardRepository boardRepository;

    public JWTRequestTest() {
    }

//    @BeforeEach
//    void before(){
//        userRepository.deleteAll();
//
//        User user = userService.save(User.builder()
//                .email("user1")
//                .password("1111")
//                .enabled(true)
//                .build());
//        userService.addAuthority(user.getUserId(), "ROLE_USER");
//    }


    private TokenBox getToken() {
        RestTemplate client = new RestTemplate();

        HttpEntity<UserLoginDto> body = new HttpEntity<>(
                UserLoginDto.builder().username("admin@test.com").password("1234").build()
        );

        System.out.println("body = " + body);
        ResponseEntity<User> resp1 = client.exchange(uri("/login"), HttpMethod.POST, body, User.class);
        return TokenBox.builder().authToken(resp1.getHeaders().get("access_token").get(0))
        .refreshToken(resp1.getHeaders().get("refresh_token").get(0)).build();
    }

    private TokenBox refreshToken(String refreshToken) {
        RestTemplate client = new RestTemplate();

        HttpEntity<UserLoginDto> body = new HttpEntity<>(
                UserLoginDto.builder().refreshToken(refreshToken).build()
        );
        ResponseEntity<User> resp1 = client.exchange(uri("/login"), HttpMethod.POST, body, User.class);
        return TokenBox.builder().authToken(resp1.getHeaders().get("access_token").get(0))
                .refreshToken(resp1.getHeaders().get("refresh_token").get(0)).build();
    }
    @DisplayName("1. hello 메시지를 받아온다.")
    @Test
    void test_1() {
        TokenBox token = getToken();

        RestTemplate client = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        System.out.println("token = " + token.getAuthToken());
        header.add(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAuthToken());
        HttpEntity body = new HttpEntity<>(null, header);
        ResponseEntity<String> resp2 = client.exchange(uri("/api/greeting"), HttpMethod.GET, body, String.class);

        assertEquals("hello", resp2.getBody());
    }


    @DisplayName("2. 토큰 만료 테스트")
    @Test
    void test_2() throws InterruptedException {
        TokenBox token = getToken();

        Thread.sleep(3000);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAuthToken());
        RestTemplate client = new RestTemplate();
        assertThrows(Exception.class, () -> {
            HttpEntity body = new HttpEntity<>(null, header);
            ResponseEntity<String> resp2 = client.exchange(uri("/api/greeting"), HttpMethod.GET, body, String.class);
        });

        token = refreshToken(token.getRefreshToken());
        HttpHeaders header2 = new HttpHeaders();
        header2.add(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAuthToken());
        HttpEntity body = new HttpEntity<>(null, header2);
        ResponseEntity<String> resp3 = client.exchange(uri("/api/greeting"), HttpMethod.GET, body, String.class);

        assertEquals("hello", resp3.getBody());
    }

    @DisplayName("")
    @Test
    @Transactional
    void test() {
        Optional<User> byEmail = userRepository.findByEmail("admin@test.com");

//        Board board = Board.builder().user(byEmail.get()).cotnents("AAAA").subject("BBBBB").build();
//        Board save = boardRepository.save(board);

        System.out.println("save = " + byEmail.toString());

    }
}
