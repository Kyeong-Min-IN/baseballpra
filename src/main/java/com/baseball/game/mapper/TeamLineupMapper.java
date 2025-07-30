package com.baseball.game.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baseball.game.dto.TeamLineup;

@Mapper
public interface TeamLineupMapper {

    // 기본 라인업 조회 (컴퓨터용)
    List<TeamLineup> findDefaultLineupByTeam(@Param("teamName") String teamName);

    // 유저 커스텀 라인업 조회
    List<TeamLineup> findCustomLineupByUserAndTeam(
            @Param("userId") String userId,
            @Param("teamName") String teamName);

    // 유저의 기존 커스텀 라인업 삭제 (새로 저장하기 전에)
    void deleteCustomLineupByUserAndTeam(
            @Param("userId") String userId,
            @Param("teamName") String teamName);

    // 커스텀 라인업 저장 (한 번에 여러 개)
    void insertCustomLineup(TeamLineup lineup);

    // 특정 유저의 모든 커스텀 라인업 조회
    List<TeamLineup> findAllCustomLineupsByUser(@Param("userId") String userId);

    // 팀별 사용 가능한 선수 목록 조회
    List<String> findAvailablePlayersByTeam(@Param("teamName") String teamName);
}