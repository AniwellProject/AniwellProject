package com.example.RSW.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/member")
public class ApiMemberController {

    @GetMapping("/myPage")
    public ResponseEntity<Member> getMyPage(HttpServletRequest req) {
        Rq rq = (Rq) req.getAttribute("rq");
        Member loginedMember = rq.getLoginedMember();

        if (loginedMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(loginedMember);
    }
}    



