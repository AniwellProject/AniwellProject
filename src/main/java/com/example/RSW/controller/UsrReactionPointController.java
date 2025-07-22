package com.example.RSW.controller;

import com.example.RSW.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.RSW.service.ArticleService;
import com.example.RSW.service.BoardService;
import com.example.RSW.service.ReactionPointService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UsrReactionPointController {

    @Autowired
    private Rq rq;

    @Autowired
    private ReactionPointService reactionPointService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ReplyService replyService;

    // ✅ 좋아요 처리
    @RequestMapping("/usr/reactionPoint/doGoodReaction")
    @ResponseBody
    public ResultData doGoodReaction(String relTypeCode, int relId, String replaceUri) {
        int actorId = rq.getLoginedMemberId();

        ResultData usersReactionRd = reactionPointService.usersReaction(actorId, relTypeCode, relId);
        int usersReaction = (int) usersReactionRd.getData1();

        if (usersReaction == 1) {
            reactionPointService.deleteGoodReactionPoint(actorId, relTypeCode, relId);

            // 댓글인 경우 reply 테이블 수치 감소
            if (relTypeCode.equals("reply")) {
                replyService.decreaseGoodRP(relId);
            }

        } else if (usersReaction == -1) {
            reactionPointService.deleteBadReactionPoint(actorId, relTypeCode, relId);
            reactionPointService.addGoodReactionPoint(actorId, relTypeCode, relId);

            if (relTypeCode.equals("reply")) {
                replyService.decreaseBadRP(relId);
                replyService.increaseGoodRP(relId);
            }

        } else {
            reactionPointService.addGoodReactionPoint(actorId, relTypeCode, relId);

            if (relTypeCode.equals("reply")) {
                replyService.increaseGoodRP(relId);
            }
        }

        int goodRP, badRP;

        if (relTypeCode.equals("reply")) {
            goodRP = replyService.getGoodRP(relId);
            badRP = replyService.getBadRP(relId);
        } else {
            goodRP = reactionPointService.getReactionPoint(relTypeCode, relId, 1);
            badRP = reactionPointService.getReactionPoint(relTypeCode, relId, -1);
        }

        return ResultData.from("S-1", "처리 완료", "goodRP", goodRP, "badRP", badRP);
    }



    // ✅ 싫어요 처리
    @RequestMapping("/usr/reactionPoint/doBadReaction")
    @ResponseBody
    public ResultData doBadReaction(String relTypeCode, int relId, String replaceUri) {
        int actorId = rq.getLoginedMemberId();

        ResultData usersReactionRd = reactionPointService.usersReaction(actorId, relTypeCode, relId);
        int usersReaction = (int) usersReactionRd.getData1();

        if (usersReaction == -1) {
            reactionPointService.deleteBadReactionPoint(actorId, relTypeCode, relId);

            if (relTypeCode.equals("reply")) {
                replyService.decreaseBadRP(relId);
            }

        } else if (usersReaction == 1) {
            reactionPointService.deleteGoodReactionPoint(actorId, relTypeCode, relId);
            reactionPointService.addBadReactionPoint(actorId, relTypeCode, relId);

            if (relTypeCode.equals("reply")) {
                replyService.decreaseGoodRP(relId);
                replyService.increaseBadRP(relId);
            }

        } else {
            reactionPointService.addBadReactionPoint(actorId, relTypeCode, relId);

            if (relTypeCode.equals("reply")) {
                replyService.increaseBadRP(relId);
            }
        }

        int goodRP, badRP;

        if (relTypeCode.equals("reply")) {
            goodRP = replyService.getGoodRP(relId);
            badRP = replyService.getBadRP(relId);
        } else {
            goodRP = articleService.getGoodRP(relId);
            badRP = articleService.getBadRP(relId);
        }

        return ResultData.from("S-1", "처리 완료", "goodRP", goodRP, "badRP", badRP);
    }



    // ✅ toggleReaction은 그대로 유지
    @PostMapping("/usr/reactionPoint/toggle")
    @ResponseBody
    public ResultData<?> toggleReaction(HttpServletRequest req, String relTypeCode, int relId) {
        return reactionPointService.toggleReaction(rq.getLoginedMemberId(), relTypeCode, relId);
    }
}