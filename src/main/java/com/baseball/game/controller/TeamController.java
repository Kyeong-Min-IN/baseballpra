package com.baseball.game.controller;

import com.baseball.game.dto.Player;
import com.baseball.game.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService service;

    @Autowired
    public TeamController(TeamService service) {
    	this.service=service;
    }

    /**
     * 팀별 선수(라인업 후보) 목록 조회
     * @param teamId 팀 ID
     * @return 해당 팀의 선수 목록
     */
    @GetMapping("/{teamId}/players")
    public List<Player> getPlayersByTeam(@PathVariable String teamId) {
        return service.getPlayersByTeam(teamId);
    }

    // 기존 registerLineup 메서드는 GameController의 setTeamLineupAndPitcher와 중복되므로 제거합니다.
    // 게임별 라인업 설정은 GameService에서 담당하는 것이 더 적절합니다.
}