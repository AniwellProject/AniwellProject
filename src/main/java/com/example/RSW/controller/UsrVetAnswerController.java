package com.example.RSW.controller;

import com.example.RSW.service.NotificationService;
import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.Qna;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.VetAnswer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/usr/vetAnswer") // 사용자(수의사)용 답변 기능 컨트롤러
public class UsrVetAnswerController {

    @Autowired
    private VetAnswerService vetAnswerService;

    @Autowired
    private QnaService qnaService;

    @Autowired
    private Rq rq;

    @Autowired
    private NotificationService notificationService;

    // 답변 등록 처리
    @PostMapping("/doWrite")
    @ResponseBody
    public String doWrite(@RequestParam("qnaId") int qnaId,
                          @RequestParam("answer") String answer) {

        Member loginedMember = rq.getLoginedMember();

        // 1. 로그인 및 수의사 권한 확인
        if (loginedMember == null || loginedMember.getAuthLevel() != 3) {
            return Ut.jsHistoryBack("F-1", "수의사만 답변할 수 있습니다.");
        }

        // 2. 질문 존재 여부 확인
        Qna qna = qnaService.getQnaById(qnaId);
        if (qna == null) {
            return Ut.jsHistoryBack("F-2", "해당 질문이 존재하지 않습니다.");
        }

        // 3. 이미 답변이 달린 질문인지 확인
        if (qna.isAnswered()) {
            return Ut.jsHistoryBack("F-3", "이미 답변이 등록된 질문입니다.");
        }

        // 4. 답변 저장
        vetAnswerService.write(qnaId, loginedMember.getId(), answer, loginedMember.getNickname());

        // 5. 질문 상태를 '답변 완료'로 변경
        qnaService.markAsAnswered(qnaId);

        // ✅ 알림 전송 (질문자 본인에게만, 수의사 자신이 아닐 경우)
        if (qna.getMemberId() != loginedMember.getId()) {
            String title = "수의사로부터 답변이 등록되었습니다.";
            String link = "/usr/qna/list";
            notificationService.send(qna.getMemberId(), title, link);
        }

        // 6. 성공 메시지 후 질문 상세로 이동
        return Ut.jsReplace("S-1", "답변이 등록되었습니다.", "/usr/qna/detail?id=" + qnaId);
    }


    // 답변 수정 처리
    @PostMapping("/doModify")
    @ResponseBody
    public Map<String, Object> doModify(@RequestParam int qnaId, @RequestParam String answer) {
        Map<String, Object> result = new HashMap<>();

        VetAnswer answer1 = vetAnswerService.findByQnaId(qnaId);
        Member loginedMember = rq.getLoginedMember();

        if (answer == null) {
            result.put("resultCode", "F-1");
            result.put("msg", "존재하지 않는 답변입니다.");
            return result;
        }

        if (answer1.getMemberId() != loginedMember.getId()) {
            result.put("resultCode", "F-2");
            result.put("msg", "권한이 없습니다.");
            return result;
        }

        vetAnswerService.modify(answer1.getId(), answer);

        result.put("resultCode", "S-1");
        result.put("msg", "수정 완료되었습니다.");
        return result;
    }




    @PostMapping("/doDelete")
    @ResponseBody
    public Map<String, Object> doDelete(@RequestParam int qnaId) {
        Map<String, Object> result = new HashMap<>();

        VetAnswer answer = vetAnswerService.findByQnaId(qnaId);
        Member loginedMember = rq.getLoginedMember();

        if (answer == null) {
            result.put("resultCode", "F-1");
            result.put("msg", "존재하지 않는 답변입니다.");
            return result;
        }

        if (answer.getMemberId() != loginedMember.getId()) {
            result.put("resultCode", "F-2");
            result.put("msg", "권한이 없습니다.");
            return result;
        }

        vetAnswerService.delete(answer.getId());
        qnaService.markAsAnsweredFalse(qnaId);

        result.put("resultCode", "S-1");
        result.put("msg", "답변이 삭제되었습니다.");
        return result;
    }



    // 수의사 전용 질문 목록 페이지
    @RequestMapping("/vetList")
    public String showVetQnaList(Model model, HttpServletResponse resp) throws IOException {
        Member loginedMember = rq.getLoginedMember();

        if (loginedMember == null || loginedMember.getAuthLevel() != 3) {
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().write("<script>alert('수의사만 접근할 수 있습니다.'); history.back();</script>");
            resp.getWriter().flush();
            return null; // 페이지 이동 없음
        }

        List<Qna> questions = qnaService.findWithoutAnswer();
        List<Qna> myAnsweredQnas = qnaService.getMyAnsweredQna(loginedMember.getId());

        model.addAttribute("myAnsweredQnas", myAnsweredQnas);
        model.addAttribute("questions", questions);
        model.addAttribute("rq", rq);

        return "usr/vetAnswer/vetList";
    }

}