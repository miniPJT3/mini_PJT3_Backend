package mini_pjt3.com.team1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/forbidden")
    public ResponseEntity<String> forbidden() {
        // 강제로 403 Forbidden 응답을 보냅니다.
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
    }

    @GetMapping("/too-many")
    public ResponseEntity<String> tooMany() {
        // 강제로 429 Too Many Requests 응답을 보냅니다.
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too Many Requests");
    }
}