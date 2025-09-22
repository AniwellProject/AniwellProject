package com.example.RSW.controller;

import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class UsrQnaController {

    @Autowired
    private Rq rq;

    @Autowired
    private QnaService qnaService;

    @Autowired
    private VetAnswerService vetAnswerService;

    // 질문 리스트 페이지 (자주 묻는 질문 + 내가 한 질문)
    @RequestMapping("/usr/qna/list")
    public String showQnaList(Model model,
                              @RequestParam(value = "selectedId", required = false) Integer selectedId) {
        List<Qna> qnas = qnaService.getPublicFaqList(); // 공개 FAQ 목록
        List<Qna> myQnas = qnaService.getUserQnaByMemberId(rq.getLoginedMemberId()); // 내가 한 질문 목록
        List<Qna> selectedQna  = qnaService.getSelectedQna(); // 선택된 질문

        // 공개 질문에 대한 수의사 답변 세팅
        for (Qna q : qnas) {
            VetAnswer a = vetAnswerService.findByQnaId(q.getId());
            if (a != null) {
                q.setAnswer(a.getAnswer());
            }
        }

        // 내가 한 질문에 대한 수의사 답변 세팅
        for (Qna q : myQnas) {
            VetAnswer a = vetAnswerService.findByQnaId(q.getId());
            if (a != null) {
                System.out.println("qnaId: " + a.getQnaId());
                System.out.println("memberId: " + a.getMemberId());
                System.out.println("vetName: " + a.getVetName());
                q.setAnswer(a.getAnswer());
                System.out.println(a);
            } else {
                System.out.println("❗답변 없음 for qnaId = " + q.getId());
            }
        }

        Member loginedMember = rq.getLoginedMember(); // 로그인 멤버 객체
        model.addAttribute("loginedMember", loginedMember);

        model.addAttribute("qnas", qnas);
        model.addAttribute("myQnas", myQnas);
        model.addAttribute("selectedQna", selectedQna);
        return "usr/qna/list"; // JSP or Thymeleaf 렌더링
    }



    // 질문 등록 폼
    @RequestMapping("/usr/qna/ask")
    public String showQnaAskForm() {
        return "usr/qna/ask";
    }

    @RequestMapping(value = "/usr/qna/doAsk", method = RequestMethod.POST)
    @ResponseBody
    public ResultData doAsk(@RequestParam String title,
                            @RequestParam String body,
                            @RequestParam(required = false, defaultValue = "false") boolean isSecret) {

        int loginedMemberId = rq.getLoginedMemberId();

        if (loginedMemberId == 0) {
            return ResultData.from("F-L", "로그인 후 이용해주세요.");
        }

        if (Ut.isEmptyOrNull(title)) return ResultData.from("F-1", "제목을 입력해주세요.");
        if (Ut.isEmptyOrNull(body)) return ResultData.from("F-2", "내용을 입력해주세요.");

        qnaService.writeUserQna(loginedMemberId, title, body, isSecret);

        return ResultData.from("S-1", "질문이 성공적으로 등록되었습니다.");
    }


    @PostMapping("/usr/qna/doModify")
    @ResponseBody
    public ResultData doModify(@RequestParam int id,
                               @RequestParam String title,
                               @RequestParam String body,
                               @RequestParam(defaultValue = "false") boolean isSecret) {

        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            return ResultData.from("F-1", "존재하지 않는 질문입니다.");
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            return ResultData.from("F-2", "수정 권한이 없습니다.");
        }

        qnaService.modifyQna(id, title, body, isSecret);

        return ResultData.from("S-1", "질문이 성공적으로 수정되었습니다.");
    }

    @RequestMapping("/usr/qna/myList")
    public String getMyQnaList(Model model) {
        List<Qna> myQnas = qnaService.getUserQnaByMemberId(rq.getLoginedMemberId());
        model.addAttribute("myQnas", myQnas);
        return "usr/qna/list :: list"; // Thymeleaf fragment 응답
    }

    @RequestMapping("/usr/qna/doDelete")
    @ResponseBody
    public ResultData doDelete(@RequestParam int id) {
        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            return ResultData.from("F-1", "존재하지 않는 질문입니다.");
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            return ResultData.from("F-2", "권한이 없습니다.");
        }

        qnaService.deleteQna(id);
        return ResultData.from("S-1", "질문이 삭제되었습니다.");
    }

    @GetMapping("/adm/qna/faqList")
    @ResponseBody
    public List<Qna> getFaqList() {
        return qnaService.getSelectedQna(); // 선택된 질문
    }

}