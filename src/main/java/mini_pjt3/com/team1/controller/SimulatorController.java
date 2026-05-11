package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.SimulationRequest;
import mini_pjt3.com.team1.dto.response.SimulationResponse;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final PaymentService paymentService;

    /**
     * 입금 시뮬레이션 검증 API
     * 실제 DB를 수정하지 않고, 입력된 정보가 유효한지 로직만 체크합니다.
     */
    @PostMapping("/validate")
    public ResponseEntity<SimulationResponse> validateSimulation(@RequestBody SimulationRequest request) {
        try {
            SimulationResponse response = paymentService.checkSimulationLogic(request);
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            // 이메일이 틀렸을 때 403 Forbidden 응답
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}