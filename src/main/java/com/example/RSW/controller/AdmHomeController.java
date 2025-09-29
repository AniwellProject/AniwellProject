package com.example.RSW.controller;

import com.example.RSW.vo.Rq;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adm/home")
public class AdmHomeController {

    private final Rq rq;

    public AdmHomeController(Rq rq) {
        this.rq = rq;
    }

    @GetMapping("/main")
    public String showAdminHome(Model model) {
        if (!rq.isAdmin()) {
            return rq.historyBackOnView("관리자만 접근 가능합니다.");
        }

        return "adm/home/main";
    }
    
    
}