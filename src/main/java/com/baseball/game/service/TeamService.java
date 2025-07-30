package com.baseball.game.service;

import java.util.List;

import com.baseball.game.dto.Player;

public interface TeamService {
	 public List<Player> getPlayersByTeam(String teamId);
}