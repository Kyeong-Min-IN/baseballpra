package com.baseball.game.service;

import java.util.List;
import com.baseball.game.dto.TeamLineup;
import com.baseball.game.dto.CustomLineupRequest;

public interface TeamLineupService {

    // 기본 라인업 조회 (컴퓨터용)
    List<TeamLineup> getDefaultLineup(String teamName);

    // 유저 커스텀 라인업 조회
    List<TeamLineup> getCustomLineup(String userId, String teamName);

    // 커스텀 라인업 저장
    void saveCustomLineup(CustomLineupRequest request);

    // 유저의 모든 커스텀 라인업 조회
    List<TeamLineup> getAllCustomLineups(String userId);

    // 팀별 사용 가능한 선수 목록 조회
    List<String> getAvailablePlayers(String teamName);
}