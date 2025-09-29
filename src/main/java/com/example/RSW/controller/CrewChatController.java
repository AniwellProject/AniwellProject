package com.example.RSW.controller;

import com.example.RSW.service.CrewChatMessageService;
import com.example.RSW.service.CrewMemerService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.PetService;
import com.example.RSW.vo.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
public class CrewChatController {

    @Autowired
    private CrewChatMessageService chatService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CrewMemerService crewMemerService;

    @Autowired
    private PetService petService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 채팅 페이지
    @RequestMapping("/usr/walkCrew/chat")
    public String showChatPage(@RequestParam int crewId, HttpSession session, Model model) {
        int loginedMemberId = (int) session.getAttribute("loginedMemberId");
        Member member = memberService.getMemberById(loginedMemberId);
        WalkCrewMember crewMember = crewMemerService.getCrewMemberById(crewId, loginedMemberId);
        Pet pet = petService.getPetsById(crewMember.getPetId());


        // 크루 참여 여부 확인
        boolean isMember = crewMemerService.isCrewMember(crewId, loginedMemberId);
        if (!isMember) {
            return "redirect:/usr/walkCrew/list"; // 참여 안했으면 크루 리스트로 이동
        }

        model.addAttribute("crewId", crewId); // 해당 크루의 id
        model.addAttribute("loginedMember", member); // 로그인 한 멤버
        model.addAttribute("pet", pet); // 크루 가입한 펫

        return "usr/walkCrew/crewChat";
    }

    // 메시지 저장 + 브로드캐스트 (WebSocket endpoint)
    @MessageMapping("/chat.send/{crewId}")
    public void sendMessage(@DestinationVariable int crewId, CrewChatMessage message) {
        message.setSentAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/crew/" + crewId, message);
    }


    // 이전 채팅 불러오기
    @RequestMapping("/usr/walkCrew/chat/api/{crewId}/messages")
    @ResponseBody
    public List<CrewChatMessage> getMessages(@PathVariable int crewId) {
        return chatService.getMessagesByCrewId(crewId);
    }
}
