package com.baseball.game.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.baseball.game.dto.TeamLineup;
import com.baseball.game.dto.CustomLineupRequest;
import com.baseball.game.service.TeamLineupService;
import lombok.Setter;

@RestController
@RequestMapping("/api/lineup")
public class TeamLineupController {

    
    private final TeamLineupService service;
    
    @Autowired
    public TeamLineupController(TeamLineupService service) {
    	this.service=service;
    }

    // 기본 라인업 조회 (컴퓨터용)
    @GetMapping("/default/{teamName}")
    public ResponseEntity<List<TeamLineup>> getDefaultLineup(@PathVariable String teamName) {
        List<TeamLineup> lineup = service.getDefaultLineup(teamName);
        return ResponseEntity.ok(lineup);
    }

    // 유저 커스텀 라인업 조회
    @GetMapping("/custom/{userId}/{teamName}")
    public ResponseEntity<List<TeamLineup>> getCustomLineup(
            @PathVariable String userId,
            @PathVariable String teamName) {
        List<TeamLineup> lineup = service.getCustomLineup(userId, teamName);
        return ResponseEntity.ok(lineup);
    }

    // 커스텀 라인업 저장
    @PostMapping("/custom")
    public ResponseEntity<String> saveCustomLineup(@RequestBody CustomLineupRequest request) {
        service.saveCustomLineup(request);
        return ResponseEntity.ok("커스텀 라인업이 저장되었습니다.");
    }

    // 유저의 모든 커스텀 라인업 조회
    @GetMapping("/custom/{userId}")
    public ResponseEntity<List<TeamLineup>> getAllCustomLineups(@PathVariable String userId) {
        List<TeamLineup> lineups = service.getAllCustomLineups(userId);
        return ResponseEntity.ok(lineups);
    }

    // 팀별 사용 가능한 선수 목록 조회
    @GetMapping("/players/{teamName}")
    public ResponseEntity<List<String>> getAvailablePlayers(@PathVariable String teamName) {
        List<String> players = service.getAvailablePlayers(teamName);
        return ResponseEntity.ok(players);
    }
}