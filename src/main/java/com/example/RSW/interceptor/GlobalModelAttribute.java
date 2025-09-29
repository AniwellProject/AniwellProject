package com.example.RSW.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.RSW.vo.Rq;

@ControllerAdvice
public class GlobalModelAttribute {
	// rq에 저장된 memberId가 전역변수가 될 수 있도록
	@Autowired
	private Rq rq;

	@ModelAttribute("rq")// 타임리프에서 rq로 객체 선언 가
	public Rq setRq() {
		return rq;
	}
}
