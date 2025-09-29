package com.example.RSW.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BeforeActionInterceptor implements HandlerInterceptor {

	@Autowired
	private Rq rq;

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {

//		Rq rq = new Rq(req, resp);

		rq.initBeforeActionInterceptor();

		// ✅ JSP에서 ${isLogined}, ${loginedMemberId} 사용할 수 있도록 setAttribute
		req.setAttribute("loginedMemberId", rq.getLoginedMemberId());
		req.setAttribute("isLogined", rq.isLogined()); // ✅ 이 줄 꼭 추가!!
		req.setAttribute("rq", rq);

		return HandlerInterceptor.super.preHandle(req, resp, handler);
	}
}