package com.example.RSW.service;

import java.util.List;

import com.example.RSW.repository.ArticleRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.ReplyRepository;
import com.example.RSW.vo.Reply;

@Service
public class ReplyService {

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationService notificationService;


    public ReplyService(ReplyRepository replyRepository) {
        this.replyRepository = replyRepository;
    }

    public List<Reply> getForPrintReplies(int loginedMemberId, String relTypeCode, int id) {
        List<Reply> replies = replyRepository.getForPrintReplies(loginedMemberId, relTypeCode, id);

        for (Reply reply : replies) {
            controlForPrintData(loginedMemberId, reply);
        }

        return replies;
    }


    private void controlForPrintData(int loginedMemberId, Reply reply) {
        if (reply == null) {
            return;
        }
        ResultData userCanModifyRd = userCanModify(loginedMemberId, reply);
        reply.setUserCanModify(userCanModifyRd.isSuccess());

        ResultData userCanDeleteRd = userCanDelete(loginedMemberId, reply);
        reply.setUserCanDelete(userCanModifyRd.isSuccess());
    }

    public ResultData userCanDelete(int loginedMemberId, Reply reply) {
        if (reply.getMemberId() != loginedMemberId) {
            return ResultData.from("F-2", Ut.f("%d번 댓글에 대한 삭제 권한이 없습니다", reply.getId()));
        }
        return ResultData.from("S-1", Ut.f("%d번 댓글을 삭제했습니다", reply.getId()));
    }

    public ResultData userCanModify(int loginedMemberId, Reply reply) {
        if (reply.getMemberId() != loginedMemberId) {
            return ResultData.from("F-2", Ut.f("%d번 댓글에 대한 수정 권한이 없습니다", reply.getId()));
        }
        return ResultData.from("S-1", Ut.f("%d번 댓글을 수정했습니다", reply.getId()), "수정된 댓글", reply);
    }

    public Reply getReply(int id) {
        return replyRepository.getReply(id);
    }

    public void modifyReply(int id, String body) {
        replyRepository.modifyReply(id, body);
    }

    public void deleteReply(int id) {
        replyRepository.deleteReply(id);
    }

    public ResultData writeReply(int loginedMemberId, String body, String relTypeCode, int relId) {
        replyRepository.writeReply(loginedMemberId, body, relTypeCode, relId);
        int id = replyRepository.getLastInsertId();

        if (relTypeCode.equals("article")) {
            Article post = articleRepository.getArticleById(relId);
            if (post != null && post.getMemberId() != loginedMemberId) {
                String nickname = memberRepository.getNicknameById(loginedMemberId);
                String message = "💬 " + nickname + "님이 회원님의 글에 댓글을 달았습니다.";
                String link = "/usr/article/detail?id=" + relId + "#reply-" + id;
                String type = "COMMENT"; // ✅ 알림 타입

                notificationService.addNotification(
                        post.getMemberId(),     // 수신자
                        loginedMemberId,        // 보낸 사람
                        type,                   // 알림 타입
                        message,
                        link
                );
            }
        }

        return ResultData.from("S-1", Ut.f("%d번 댓글이 등록되었습니다.", id), "등록된 댓글의 id", id);
    }


    public void increaseGoodRP(int replyId) {
        replyRepository.increaseGoodReactionPoint(replyId);
    }
    public void decreaseGoodRP(int replyId) {
        replyRepository.decreaseGoodReactionPoint(replyId);
    }
    public void increaseBadRP(int replyId) {
        replyRepository.increaseBadReactionPoint(replyId);
    }
    public void decreaseBadRP(int replyId) {
        replyRepository.decreaseBadReactionPoint(replyId);
    }

    public int getGoodRP(int replyId) {
        return replyRepository.getGoodRP(replyId);
    }

    public int getBadRP(int replyId) {
        return replyRepository.getBadRP(replyId);
    }
}