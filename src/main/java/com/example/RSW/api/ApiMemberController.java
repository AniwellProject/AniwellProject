package com.example.RSW.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.RSW.service.MemberService;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/member")
public class ApiMemberController {

	@Autowired
	private MemberService memberService;

//로그인된 유저 api
	@GetMapping("/myPage")
	public ResponseEntity<Member> getMyPage(HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		Member loginedMember = rq.getLoginedMember();

		if (loginedMember == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		return ResponseEntity.ok(loginedMember);
	}

	// id기준 유저 api
	@GetMapping("/getUsrInfo")
	public ResponseEntity<Member> getUsrInfo(HttpServletRequest req) {

		Rq rq = (Rq) req.getAttribute("rq");
		int loginedMemberId = rq.getLoginedMemberId();
		System.out.println(loginedMemberId);
		
		Member member = memberService.getMemberById(loginedMemberId);

		return ResponseEntity.ok(member);
	}


}
