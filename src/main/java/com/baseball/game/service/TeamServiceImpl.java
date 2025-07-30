package com.baseball.game.service;

import com.baseball.game.dto.Player;
import com.baseball.game.mapper.TeamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import lombok.Setter;
@Service
public class TeamServiceImpl implements TeamService {
    @Setter(onMethod_ = @Autowired)
    private TeamMapper mapper;

    @Override
    public List<Player> getPlayersByTeam(String teamId) {
        return mapper.selectPlayersByTeam(teamId);
    }
    // registerLineup 메서드 삭제
}