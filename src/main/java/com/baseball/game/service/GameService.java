// src/main/java/com/baseball/game/service/GameService.java
package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import java.util.List;
import java.util.Map;

public interface GameService {
    GameDto createGame(String homeTeam, String awayTeam, int maxInning, boolean isUserOffense);
    GameDto getGame(String gameId);
    String batterSwing(String gameId, Boolean swing, Double timing);
    String pitcherThrow(String gameId, String pitchType);
    GameDto nextInning(String gameId);
    GameDto endGame(String gameId);
    void advanceRunners(String gameId, Integer bases);
    String getGameStats(String gameId);

    /**
     * 특정 팀의 타순 및 선발 투수를 설정합니다.
     * @param gameId 게임 ID
     * @param teamName 설정할 팀 이름 (홈팀 또는 원정팀)
     * @param battingOrderPlayerNames 타순에 포함될 선수 이름 리스트
     * @param startingPitcherName 선발 투수 이름
     */
    void setTeamLineupAndPitcher(String gameId, String teamName, List<String> battingOrderPlayerNames, String startingPitcherName);

    /**
     * (컴퓨터 팀을 위한 별도 명명법, 실제 로직은 setTeamLineupAndPitcher와 동일)
     * @param gameId 게임 ID
     * @param teamName 설정할 팀 이름 (홈팀 또는 원정팀)
     * @param battingOrderPlayerNames 타순에 포함될 선수 이름 리스트
     * @param startingPitcherName 선발 투수 이름
     */
    void setComputerLineupAndPitcher(String gameId, String teamName, List<String> battingOrderPlayerNames, String startingPitcherName);

    // 기타 GameServiceImpl에 protected로 선언된 메서드들은 인터페이스에 노출시키지 않습니다.
    // protected void checkCount(GameDto game);
    // protected void advanceBattingOrder(GameDto game);
    // protected void checkGameOver(GameDto game);
    // protected void handleScore(GameDto game, int score);
}