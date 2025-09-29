package com.example.RSW.controller;

import com.example.RSW.service.MemberService;
import com.example.RSW.service.NotificationService;
import com.example.RSW.service.VetCertificateService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/adm/member")
public class AdmMemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private VetCertificateService vetCertificateService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private Rq rq;



    @PostMapping("/changeVetCertStatus")
    @ResponseBody
    public ResultData<?> changeVetCertStatus(@RequestParam int memberId, @RequestParam int approved) {
        vetCertificateService.updateApprovalStatusByMemberId(memberId, approved);

        if (approved == 1) {
            memberService.updateAuthLevel(memberId, 3);
        } else if (approved == 2) {
            memberService.updateAuthLevel(memberId, 1);
        }

        Member loginedMember = rq.getLoginedMember();

        if (loginedMember == null || loginedMember.getAuthLevel() != 7) {
            return ResultData.from("F-1", "관리자만 접근할 수 있습니다.");
        }

        if (rq.getLoginedMemberId() == memberId) {
            Member updatedMember = memberService.getMemberById(memberId);
            rq.login(updatedMember);
        }

        String title = (approved == 1) ? "수의사 인증이 승인되었습니다." : "수의사 인증이 거절되었습니다.";
        String type = (approved == 1) ? "VET_APPROVED" : "VET_REJECTED";
        String link = "";

        int adminId = rq.getLoginedMemberId();
        notificationService.addNotification(memberId, adminId, type, title, link);

        return ResultData.from("S-1", title);
    }

    @PostMapping("/changeAdminStatus")
    @ResponseBody
    public ResultData<?> changeAdminStatus(@RequestParam int memberId, @RequestParam boolean promote) {
        // 1. 권한 변경 값 설정
        int newLevel = promote ? 7 : 1;

        // 2. 관리자 최소 유지 조건: 해제할 때만 검사
        if (!promote) {
            int adminCount = memberService.countByAuthLevel(7); // 전체 관리자 수 조회
            if (adminCount <= 1) {
                return ResultData.from("F-1", "❗ 최소 1명의 관리자는 유지되어야 합니다.");
            }
        }
        // 3. 본인이 관리자이면 해제 못하게 막기
        if (!promote && rq.getLoginedMemberId() == memberId) {
            return ResultData.from("F-2", "본인의 관리자 권한은 스스로 해제할 수 없습니다.");
        }

        Member loginedMember = rq.getLoginedMember();

        if (loginedMember == null || loginedMember.getAuthLevel() != 7) {
            return ResultData.from("F-3", "관리자만 접근할 수 있습니다.");
        }

        // 4. 권한 업데이트
        memberService.updateAuthLevel(memberId, newLevel);

        // 5. 본인이면 세션 갱신
        if (rq.getLoginedMemberId() == memberId) {
            Member updated = memberService.getMemberById(memberId);
            rq.login(updated);
        }

        String title = (promote) ? "관리자 권한이 부여되었습니다." : "관리자 권한이 해제되었습니다..";
        String type = (promote) ? "PROMOTE_APPROVED" : "PROMOTE_REJECTED";
        String link = "/adm/article/list";

        int adminId = rq.getLoginedMemberId();
        notificationService.addNotification(memberId, adminId, type, title, link);

        // 6. 응답 메시지 전송
        String msg = promote ? "✅ 관리자 권한이 부여되었습니다." : "❌ 관리자 권한이 해제되었습니다.";
        return ResultData.from("S-1", msg);
    }


}