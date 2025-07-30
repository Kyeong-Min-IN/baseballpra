package com.baseball.game.service;

import com.baseball.game.dto.MemberDto;
import java.util.Map;
public interface MemberService {
    boolean login(String id, String pw);

    boolean checkId(String id);

    void register(MemberDto memberDto);

    MemberDto getMember(String id);

    Map<String, Object> loginProcess(MemberDto memberDto);

    Map<String, Object> registerProcess(MemberDto memberDto);
}