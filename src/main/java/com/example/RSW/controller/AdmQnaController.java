package com.example.RSW.controller;

import com.example.RSW.service.NotificationService;
import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/adm/qna") // 관리자용 QnA 관리 컨트롤러
public class AdmQnaController {

    @Autowired
    private Rq rq; // 로그인 정보 객체

    @Autowired
    private QnaService qnaService; // QnA 관련 서비스

    @Autowired
    private VetAnswerService vetAnswerService; // 수의사 답변 관련 서비스

    @Autowired
    private NotificationService notificationService;


    // QnA 상세 페이지
    @GetMapping("/detailData")
    @ResponseBody
    public Map<String, Object> getQnaDetailData(@RequestParam int id) {
        Map<String, Object> result = new HashMap<>();
        result.put("qna", qnaService.findById(id));
        result.put("answer", vetAnswerService.findByQnaId(id));
        return result;
    }



    @PostMapping("/doUpdateAnswer")
    @ResponseBody
    public Map<String, Object> doUpdateAnswer(@RequestParam int id, @RequestParam String answer) {
        vetAnswerService.update(id, answer);
        return Map.of("resultCode", "S-1", "msg", "답변이 수정되었습니다.");
    }


    @PostMapping("/doDeleteAnswer")
    @ResponseBody
    public Map<String, Object> doDeleteAnswer(@RequestParam int id) {
        int qnaId = vetAnswerService.getQnaIdByAnswerId(id);

        Qna qna = qnaService.getQnaById(qnaId);
        qnaService.setFalse(qnaId);
        vetAnswerService.delete(id);
        return Map.of(
                "resultCode", "S-1",
                "msg", "답변이 삭제되었습니다.",
                "qnaId", qnaId
        );
    }

    // QnA 본문 수정 처리
    @PostMapping("/doModify")
    public String doModify(@RequestParam int id,
                           @RequestParam String title,
                           @RequestParam String body) {
        qnaService.modify(id, title, body); // 제목/본문 수정
        return "redirect:/adm/qna/detail?id=" + id; // 상세로 리다이렉트
    }

    // QnA 삭제 처리
    @PostMapping("/doDelete")
    @ResponseBody
    public Map<String, Object> doDelete(@RequestParam int id) {
        Map<String, Object> response = new HashMap<>();

        try {
            qnaService.delete(id);
            response.put("resultCode", "S-1");
            response.put("msg", "질문이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            response.put("resultCode", "F-1");
            response.put("msg", "삭제 중 오류가 발생했습니다.");
        }

        return response;
    }

    // FAQ 등록 처리
    @PostMapping("/doWrite")
    @ResponseBody
    public ResultData doWrite(@RequestParam String title, @RequestParam String body) {
        int memberId = rq.getLoginedMemberId();
        qnaService.writeFaq(memberId, title, body); // 내부에서 isFaq = true 저장
        return ResultData.from("S-1", "FAQ 등록 성공");
    }
}