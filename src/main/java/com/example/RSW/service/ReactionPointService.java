package com.example.RSW.service;

import com.example.RSW.repository.MemberRepository;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.BoardRepository;
import com.example.RSW.repository.ReactionPointRepository;
import com.example.RSW.vo.Board;
import com.example.RSW.vo.ResultData;

@Service
public class ReactionPointService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ReactionPointRepository reactionPointRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private NotificationService notificationService;


    public int getReactionPoint(String relTypeCode, int relId, int point) {
        return reactionPointRepository.getReactionPoint(relTypeCode, relId, point);
    }

    public ReactionPointService(ReactionPointRepository reactionPointRepository) {
        this.reactionPointRepository = reactionPointRepository;
    }

    public ResultData usersReaction(int loginedMemberId, String relTypeCode, int relId) {
        if (loginedMemberId == 0) {
            return ResultData.from("F-L", "로그인 하고 써야해");
        }

        int sum = reactionPointRepository.getSumReactionPoint(loginedMemberId, relTypeCode, relId);

        if (sum != 0) {
            return ResultData.from("F-1", "추천 불가능", "sumReactionPointByMemberId", sum);
        }

        return ResultData.from("S-1", "추천 가능", "sumReactionPointByMemberId", sum);
    }

    public ResultData addGoodReactionPoint(int loginedMemberId, String relTypeCode, int relId) {
        int affectedRow = reactionPointRepository.addGoodReactionPoint(loginedMemberId, relTypeCode, relId);

        if (affectedRow != 1) {
            return ResultData.from("F-1", "좋아요 실패");
        }

        if (relTypeCode.equals("article")) {
            articleService.increaseGoodReactionPoint(relId);
            // 💡 좋아요 알림 한 번만 여기서 처리
            Article article = articleService.getArticleById(relId);
            if (article != null && article.getMemberId() != loginedMemberId) {
                Member sender = memberService.getMemberById(loginedMemberId);
                String nickname = sender.getNickname();
                String title = nickname + "님이 게시글을 좋아했습니다.";
                String link = "/usr/article/detail?id=" + relId;
                String type = "POST_LIKE";

                notificationService.addNotification(
                        article.getMemberId(),     // 수신자
                        loginedMemberId,           // 보낸 사람
                        type,                      // 알림 타입
                        title,                     // 메시지
                        link                       // 링크
                );
            }
        }

        return ResultData.from("S-1", "좋아요!");
    }


    public ResultData addBadReactionPoint(int loginedMemberId, String relTypeCode, int relId) {
        int affectedRow = reactionPointRepository.addBadReactionPoint(loginedMemberId, relTypeCode, relId);

        if (affectedRow != 1) {
            return ResultData.from("F-1", "싫어요 실패");
        }

        if (relTypeCode.equals("article")) {
            articleService.increaseBadReactionPoint(relId);
        }

        return ResultData.from("S-1", "싫어요!");
    }

    public ResultData deleteGoodReactionPoint(int loginedMemberId, String relTypeCode, int relId) {
        reactionPointRepository.deleteReactionPoint(loginedMemberId, relTypeCode, relId);

        if (relTypeCode.equals("article")) {
            articleService.decreaseGoodReactionPoint(relId);
        }

        return ResultData.from("S-1", "좋아요 취소 됨");
    }

    public ResultData deleteBadReactionPoint(int loginedMemberId, String relTypeCode, int relId) {
        reactionPointRepository.deleteReactionPoint(loginedMemberId, relTypeCode, relId);

        if (relTypeCode.equals("article")) {
            articleService.decreaseBadReactionPoint(relId);
        }

        return ResultData.from("S-1", "싫어요 취소 됨");
    }

    public boolean isAlreadyAddGoodRp(int memberId, int relId, String relTypeCode) {
        int sum = reactionPointRepository.getSumReactionPoint(memberId, relTypeCode, relId);
        return sum > 0;
    }

    public boolean isAlreadyAddBadRp(int memberId, int relId, String relTypeCode) {
        int sum = reactionPointRepository.getSumReactionPoint(memberId, relTypeCode, relId);
        return sum < 0;
    }

    public ResultData<?> toggleReaction(int loginedMemberId, String relTypeCode, int relId) {
        if (loginedMemberId == 0) {
            return ResultData.from("F-1", "로그인이 필요합니다.");
        }

        boolean isAlreadyLiked = isAlreadyAddGoodRp(loginedMemberId, relId, relTypeCode);

        if (isAlreadyLiked) {
            // 좋아요 취소 시 알림 없음
            return deleteGoodReactionPoint(loginedMemberId, relTypeCode, relId);
        } else {
            // 좋아요 등록 시 알림은 addGoodReactionPoint 내부에서 처리됨
            return addGoodReactionPoint(loginedMemberId, relTypeCode, relId);
        }
    }
}
