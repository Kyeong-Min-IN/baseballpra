package com.baseball.game.service;

import com.baseball.game.dto.MemberDto;
import com.baseball.game.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.Setter;
import com.baseball.game.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class MemberServiceImpl implements MemberService {
    @Setter(onMethod_ = @Autowired)
    private MemberMapper memberMapper;

    private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

    @Override
    public boolean login(String id, String pw) {
        return memberMapper.login(id, pw);
    }

    @Override
    public boolean checkId(String id) {
        return memberMapper.checkId(id);
    }

    @Override
    public void register(MemberDto memberDto) {
        memberMapper.register(memberDto.getId(), memberDto.getPw(), memberDto.getTeam());
    }

    @Override
    public MemberDto getMember(String id) {
        // memberMapper.member(id)는 select * from member where id=#{Id} 쿼리임
        // 반환 타입이 MemberDto와 일치해야 함
        return memberMapper.member(id);
    }

    @Override
    public Map<String, Object> loginProcess(MemberDto memberDto) {
        logger.info("로그인 요청: id={}", memberDto.getId());
        Map<String, Object> response = new HashMap<>();
        // 입력값 검증
        if (memberDto.getId() == null || memberDto.getId().trim().isEmpty()) {
            throw new ValidationException("아이디는 필수입니다.");
        }
        if (memberDto.getPw() == null || memberDto.getPw().trim().isEmpty()) {
            throw new ValidationException("비밀번호는 필수입니다.");
        }
        try {
            boolean success = login(memberDto.getId(), memberDto.getPw());
            if (success) {
                response.put("success", true);
                response.put("message", "로그인 성공");
                logger.info("로그인 성공: id={}", memberDto.getId());
            } else {
                response.put("success", false);
                response.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
                logger.warn("로그인 실패: id={}", memberDto.getId());
            }
        } catch (Exception e) {
            logger.error("로그인 처리 중 오류 발생: id={}", memberDto.getId(), e);
            response.put("success", false);
            response.put("message", "로그인 처리 중 오류가 발생했습니다.");
        }
        return response;
    }

    @Override
    public Map<String, Object> registerProcess(MemberDto memberDto) {
        logger.info("회원가입 요청: id={}", memberDto.getId());
        Map<String, Object> response = new HashMap<>();
        // 입력값 검증
        if (memberDto.getId() == null || memberDto.getId().trim().isEmpty()) {
            throw new ValidationException("아이디는 필수입니다.");
        }
        if (memberDto.getPw() == null || memberDto.getPw().trim().isEmpty()) {
            throw new ValidationException("비밀번호는 필수입니다.");
        }
        if (memberDto.getTeam() == null || memberDto.getTeam().trim().isEmpty()) {
            throw new ValidationException("팀명은 필수입니다.");
        }
        try {
            if (checkId(memberDto.getId())) {
                response.put("success", false);
                response.put("message", "이미 존재하는 아이디입니다.");
                logger.warn("회원가입 실패 - 중복 아이디: id={}", memberDto.getId());
                return response;
            }
            register(memberDto);
            response.put("success", true);
            response.put("message", "회원가입 성공");
            logger.info("회원가입 성공: id={}", memberDto.getId());
        } catch (Exception e) {
            logger.error("회원가입 처리 중 오류 발생: id={}", memberDto.getId(), e);
            response.put("success", false);
            response.put("message", "회원가입 처리 중 오류가 발생했습니다.");
        }
        return response;
    }
}