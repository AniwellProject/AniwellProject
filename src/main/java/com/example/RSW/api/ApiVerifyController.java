package com.example.RSW.api;

import com.example.RSW.service.SmsVerifyService;
import com.example.RSW.vo.ResultData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/verify/sms")
@RequiredArgsConstructor
public class ApiVerifyController {

    private final SmsVerifyService sms;

    @PostMapping("/send")
    public ResultData<?> send(@RequestBody Map<String, String> body) {
        return sms.sendCode(body.get("phone"));
    }

    @PostMapping("/confirm")
    public ResultData<?> confirm(@RequestBody Map<String, String> body) {
        return sms.confirmCode(body.get("phone"), body.get("code"));
    }
}
