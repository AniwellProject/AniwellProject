package com.example.RSW.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NeedLogoutInterceptor implements HandlerInterceptor {

    @Autowired
    private Rq rq;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        String uri = req.getRequestURI();

        // ✅ 로그아웃 URL 예외 처리
        if (uri.equals("/usr/member/logout")) {
            return true;
        }

        // ✅ 정적 리소스 요청은 예외 처리
        if (uri.startsWith("/resource/") || uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/img/")) {
            return true;
        }

        // ✅ 로그인 상태에서 로그인 관련 요청 차단
        if (rq.isLogined() && uri.equals("/usr/member/login")) {
            rq.printHistoryBack("로그아웃 후 이용하세요.(NeedLogoutInterceptor)");
            return false;
        }


        return true;
    }
}
