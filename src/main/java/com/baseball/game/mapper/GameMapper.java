package com.baseball.game.mapper;

import java.util.List;
import com.baseball.game.dto.*;

public interface GameMapper {
	// 게임 정보
	void insertGame(GameDto game);

	GameDto selectGameById(String gameId);

	void updateGame(GameDto game);

	void deleteGame(String gameId);

	// 팀/선수
	List<Team> selectAllTeams();

	Team selectTeamById(String teamId);

	List<Batter> selectBattersByTeam(String teamId);

	List<Pitcher> selectPitchersByTeam(String teamId);

	// 주자
	List<Batter> selectRunnersByGame(String gameId);
}
