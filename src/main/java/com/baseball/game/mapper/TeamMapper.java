package com.baseball.game.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baseball.game.dto.Player;

@Mapper
public interface TeamMapper {
    List<Player> selectPlayersByTeam(String teamId);

    void registerLineup(String gameId, List<String> battingOrder, String StartPitcher);

}
