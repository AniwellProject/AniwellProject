package com.example.RSW.controller;

import com.example.RSW.service.CrewChatMessageService;
import com.example.RSW.service.MemberService;
import com.example.RSW.vo.CrewChatMessage;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequestMapping("/api/chat")
public class CrewChatApiController {
	@Autowired
	private MemberService memberService;

	private final CrewChatMessageService chatService;

	public CrewChatApiController(CrewChatMessageService chatService) {
		this.chatService = chatService;
	}

	// 해당 채팅방의 채팅 목록 넘기기
	@GetMapping("/{crewId}/messages")
	public List<CrewChatMessage> getChatMessages(@PathVariable int crewId) {
		return chatService.getMessagesByCrewId(crewId);
	}

	@GetMapping("/room")
	public String showChatRoom(@RequestParam int crewId, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		int memberId = rq.getLoginedMemberId();

		model.addAttribute("crewId", crewId);
		model.addAttribute("loginedMember", memberService.getMemberById(memberId));

		/*
		 * Pet pet = petService.getPetByMemberId(memberId); // 사용자 반려동물 사진
		 * model.addAttribute("pet", pet);
		 */

		return "usr/walkCrew/crewChat";
	}

}
