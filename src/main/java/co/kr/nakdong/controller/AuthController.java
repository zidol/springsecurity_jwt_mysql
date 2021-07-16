package co.kr.nakdong.controller;

import co.kr.nakdong.config.JWTUtil;
import co.kr.nakdong.dto.LogOutDto;
import co.kr.nakdong.entity.author.RefreshToken;
import co.kr.nakdong.exception.TokenRefreshException;
import co.kr.nakdong.repository.UserRepository;
import co.kr.nakdong.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final RefreshTokenService refreshTokenService;
  private final JWTUtil jwtUtil;


//  @PostMapping("/signup")
//  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
//    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
//    }
//
//    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
//    }
//
//    // Create new user's account
//    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
//        encoder.encode(signUpRequest.getPassword()));
//
//    Set<String> strRoles = signUpRequest.getRole();
//    Set<Role> roles = new HashSet<>();
//
//    if (strRoles == null) {
//      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
//          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//      roles.add(userRole);
//    } else {
//      strRoles.forEach(role -> {
//        switch (role) {
//        case "admin":
//          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
//              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//          roles.add(adminRole);
//
//          break;
//        case "mod":
//          Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
//              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//          roles.add(modRole);
//
//          break;
//        default:
//          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
//              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//          roles.add(userRole);
//        }
//      });
//    }
//
//    user.setRoles(roles);
//    userRepository.save(user);
//
//    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
//  }

  @PostMapping("/refreshToken")
  public ResponseEntity<?> refreshtoken(HttpServletRequest request) {

    Optional<String> refreshToken = Arrays.stream(request.getCookies())
            .filter(cookie -> "refreshToken".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findAny();

    System.out.println("refreshToken = " + refreshToken);
//    String requestRefreshToken = request.getRefreshToken();
//
    return refreshTokenService.findByToken(refreshToken.get())
        .map(refreshTokenService::verifyExpiration)
        .map(RefreshToken::getUser)
        .map(user -> {
//          String token = jwtUtils.generateTokenFromUsername(user.getUsername());
          String accessToken = JWTUtil.makeAuthToken(user);

          return ResponseEntity.ok(accessToken);
        })
        .orElseThrow(() -> new TokenRefreshException(refreshToken.get(),
            "Refresh token is not in database!"));
  }
  
  @PostMapping("/logout")
  public ResponseEntity<?> logoutUser(@Valid @RequestBody LogOutDto logOutDto) {
    refreshTokenService.deleteByUserId(logOutDto.getUserName());
    return ResponseEntity.ok("Log out successful!");
  }

}
