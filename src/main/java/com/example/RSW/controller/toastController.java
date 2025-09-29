package com.example.RSW.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.RSW.vo.ResultData;

@Controller
@RequestMapping("/toast")
public class toastController {
	
	@PostMapping("/doLogin")
	@ResponseBody
    public ResultData doLogin() {
        return ResultData.from("S-1", "로그인 되었습니다!");
    }
	
	@PostMapping("/doLogout")
	@ResponseBody
    public ResultData doLogout() {
        return ResultData.from("S-1", "로그아웃 되었습니다!");
    }
	
	@PostMapping("/doDelete")
	@ResponseBody
    public ResultData doDelete() {
        return ResultData.from("S-1", "삭제 되었습니다!");
    }
	
	@PostMapping("/doModify")
	@ResponseBody
    public ResultData doModify() {
        return ResultData.from("S-1", "수정 되었습니다!");
    }

	@PostMapping("/doSave")
	@ResponseBody
    public ResultData doSave() {
        return ResultData.from("S-1", "저장 되었습니다!");
    }
	
	@PostMapping("/doNeedLogin")
	@ResponseBody
    public ResultData doNeedLogin() {
        return ResultData.from("S-1", "로그인 이후 이용해주세요!");
    }

	
}
