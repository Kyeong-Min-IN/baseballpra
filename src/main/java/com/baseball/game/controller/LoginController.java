package com.baseball.game.controller;

import com.baseball.game.dto.MemberDto;
import com.baseball.game.service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = "*")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    
    private final MemberService service;
    @Autowired
    public LoginController(MemberService service) {
    	this.service=service;
    }

    // 로그인
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody MemberDto memberDto) {
        // 모든 검증 및 로직을 서비스로 위임
        return service.loginProcess(memberDto);
    }

    // 회원가입
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody MemberDto memberDto) {
        // 모든 검증 및 로직을 서비스로 위임
        return service.registerProcess(memberDto);
    }
}
