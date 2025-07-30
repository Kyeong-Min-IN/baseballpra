package com.baseball.game.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baseball.game.dto.TeamLineup;
import com.baseball.game.dto.CustomLineupRequest;
import com.baseball.game.mapper.TeamLineupMapper;
import lombok.Setter;

@Service
public class TeamLineupServiceImpl implements TeamLineupService {

    @Setter(onMethod_ = @Autowired)
    private TeamLineupMapper teamLineupMapper;

    @Override
    public List<TeamLineup> getDefaultLineup(String teamName) {
        return teamLineupMapper.findDefaultLineupByTeam(teamName);
    }

    @Override
    public List<TeamLineup> getCustomLineup(String userId, String teamName) {
        return teamLineupMapper.findCustomLineupByUserAndTeam(userId, teamName);
    }

    @Override
    @Transactional
    public void saveCustomLineup(CustomLineupRequest request) {
        // 기존 커스텀 라인업 삭제
        teamLineupMapper.deleteCustomLineupByUserAndTeam(request.getUserId(), request.getTeamName());

        // 새로운 커스텀 라인업 저장
        for (CustomLineupRequest.LineupPosition position : request.getLineup()) {
            TeamLineup lineup = new TeamLineup();
            lineup.setTeamName(request.getTeamName());
            lineup.setUserId(request.getUserId());
            lineup.setPosition(position.getPosition());
            lineup.setPlayerName(position.getPlayerName());
            lineup.setPlayerId(position.getPlayerId());

            teamLineupMapper.insertCustomLineup(lineup);
        }
    }

    @Override
    public List<TeamLineup> getAllCustomLineups(String userId) {
        return teamLineupMapper.findAllCustomLineupsByUser(userId);
    }

    @Override
    public List<String> getAvailablePlayers(String teamName) {
        return teamLineupMapper.findAvailablePlayersByTeam(teamName);
    }
}