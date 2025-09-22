package com.example.RSW.controller;

import com.example.RSW.service.NotificationService;
import com.example.RSW.service.MemberService;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.Notification;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/adm/notification")
public class AdmNotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private Rq rq;

 // 관리자용 알림 리스트
    @GetMapping("/list")
    public String showNotificationList(Model model) {
        int adminId = rq.getLoginedMemberId();

        List<Notification> notifications = notificationService.findByMemberId(adminId);

        model.addAttribute("notifications", notifications);
        return "adm/notification/list";
    }


    // 알림 삭제 (단일)
    @PostMapping("/delete")
    @ResponseBody
    public ResultData<?> deleteNotification(@RequestParam int id) {
        notificationService.deleteById(id);
        return ResultData.from("S-1", "알림이 삭제되었습니다.");
    }

    // 알림 전체 삭제
    @PostMapping("/deleteAll")
    @ResponseBody
    public ResultData<?> deleteAll() {
        int adminId = rq.getLoginedMemberId();
        notificationService.deleteByMemberId(adminId);
        return ResultData.from("S-1", "모든 알림이 삭제되었습니다.");
    }


}
