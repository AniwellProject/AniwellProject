package com.example.RSW.api;

import com.example.RSW.service.EmailVerificationService;
import com.example.RSW.vo.ResultData;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verify/email")
@RequiredArgsConstructor
@Validated
public class ApiEmailVerifyController {

    private final EmailVerificationService emailVerificationService;

    /** 인증코드 전송 */
    @PostMapping("/send")
    public ResultData<?> send(@Valid @RequestBody SendReq req) {
        return emailVerificationService.sendCode(req.emailTrimmed(), req.purposeOrDefault());
    }

    /** 인증코드 확인 */
    @PostMapping("/check")
    public ResultData<?> check(@Valid @RequestBody CheckReq req, HttpSession session) {
        return emailVerificationService.verifyCode(req.txIdTrimmed(), req.codeTrimmed(), req.purposeOrDefault(), session);
    }

    // === DTOs ===
    @Data
    public static class SendReq {
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일을 입력해 주세요.")
        private String email;

        /** 예: "join" (목적 구분 – 선택값) */
        private String purpose;

        public String emailTrimmed() {
            return email == null ? "" : email.trim();
        }
        public String purposeOrDefault() {
            return (purpose == null || purpose.isBlank()) ? "join" : purpose.trim();
        }
    }

    @Data
    public static class CheckReq {
        @NotBlank(message = "txId가 없습니다.")
        private String txId;

        @NotBlank(message = "코드를 입력해 주세요.")
        private String code;

        private String purpose;

        public String txIdTrimmed() { return txId == null ? "" : txId.trim(); }
        public String codeTrimmed() { return code == null ? "" : code.trim(); }
        public String purposeOrDefault() {
            return (purpose == null || purpose.isBlank()) ? "join" : purpose.trim();
        }
    }
}
