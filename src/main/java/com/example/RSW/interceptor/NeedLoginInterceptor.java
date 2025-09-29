package com.example.RSW.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NeedLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private Rq rq;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        if (!rq.isLogined()) {
            // 로그인 페이지로 리다이렉트 (예: /usr/member/login)
            resp.sendRedirect("/usr/member/login?redirectUrl=" + req.getRequestURI());
            return false;
        }

        return true;
    }

}