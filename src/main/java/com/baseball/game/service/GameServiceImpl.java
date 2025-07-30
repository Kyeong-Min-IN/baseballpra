// src/main/java/com/baseball/game/service/GameServiceImpl.java
package com.baseball.game.service;

import com.baseball.game.dto.GameDto;
import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.util.GameLogicUtil;
import com.baseball.game.exception.GameNotFoundException;
import com.baseball.game.exception.InvalidGameStateException;
import com.baseball.game.exception.ValidationException;
import com.baseball.game.repository.GameRepository; // 실제 DB 연동 시 필요
import com.baseball.game.mapper.BatterMapper;
import com.baseball.game.mapper.PitcherMapper;
import com.baseball.game.mapper.TeamLineupMapper; // 라인업 조회를 위해 필요 (현재는 사용하지 않음, 필요 시 추가)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap; // Map 초기화를 위해 추가
import java.util.stream.Collectors;

import lombok.Setter;

@Service
@Transactional
public class GameServiceImpl implements GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

    @Setter(onMethod_ = @Autowired)
    private GameRepository gameRepository; // 실제 DB 연동 시 사용 예정

    @Setter(onMethod_ = @Autowired)
    private BatterMapper batterMapper;

    @Setter(onMethod_ = @Autowired)
    private PitcherMapper pitcherMapper;

    // 메모리 기반 (임시, Redis 연동 후 제거 예정)
    private Map<String, GameDto> games = new HashMap<>();

    @Override
    @Transactional
    public GameDto createGame(String homeTeam, String awayTeam, int maxInning, boolean isUserOffense) {
        // 팀 검증 (실제 팀 데이터를 조회하는 로직 필요)
        if (homeTeam == null || awayTeam == null || homeTeam.trim().isEmpty() || awayTeam.trim().isEmpty()) {
            throw new ValidationException("홈팀과 원정팀 이름은 필수입니다.");
        }
        if (homeTeam.equals(awayTeam)) {
            throw new ValidationException("홈팀과 원정팀은 동일할 수 없습니다.");
        }
        if (maxInning <= 0) {
            throw new ValidationException("최대 이닝 수는 1 이상이어야 합니다.");
        }

        GameDto newGame = new GameDto();
        newGame.setGameId(UUID.randomUUID().toString()); // 고유한 게임 ID 생성
        newGame.setHomeTeam(homeTeam);
        newGame.setAwayTeam(awayTeam);
        newGame.setMaxInning(maxInning);
        newGame.setIsUserOffense(isUserOffense);
        newGame.setInning(1);
        newGame.setTop(true);
        newGame.setOut(0);
        newGame.setStrike(0);
        newGame.setBall(0);
        newGame.setHomeScore(0);
        newGame.setAwayScore(0);
        GameLogicUtil.resetBases(newGame); // 베이스 초기화
        newGame.setGameOver(false);
        newGame.setWinner(null);

        // 초기 타자 및 투수 설정 (이 부분은 라인업 설정 API 호출 후 이루어져야 합니다.)
        // 게임 생성 시에는 초기화만 하고, 라인업 설정 시 실제 선수 객체를 매핑합니다.
        newGame.setCurrentBatter(null);
        newGame.setCurrentPitcher(null);
        newGame.setBattingOrder(new ArrayList<>());
        newGame.setPitcherList(new ArrayList<>());
        newGame.setStartingPitcher(null);
        newGame.setCurrentBatterIndex(0);


        games.put(newGame.getGameId(), newGame);
        logger.info("Created game with ID: {}", newGame.getGameId());

        // 게임 생성 시 DB 저장 (Redis 등)
        // gameRepository.save(newGame);
        return newGame;
    }

    @Override
    public GameDto getGame(String gameId) {
        GameDto game = games.get(gameId); // 실제 DB 연동 시 gameRepository.findById(gameId)
        if (game == null) {
            throw new GameNotFoundException("게임 ID: " + gameId + "를 찾을 수 없습니다.");
        }
        return game;
    }

    @Override
    @Transactional
    public String batterSwing(String gameId, Boolean swing, Double timing) {
        GameDto game = getGame(gameId);

        if (game.isGameOver()) {
            throw new InvalidGameStateException("게임이 이미 종료되었습니다.");
        }
        if (game.getCurrentBatter() == null || game.getCurrentPitcher() == null) {
            throw new InvalidGameStateException("현재 타자 또는 투수가 설정되지 않았습니다. 라인업을 먼저 설정해주세요.");
        }
        if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
            throw new InvalidGameStateException("이미 3아웃입니다. 다음 이닝으로 진행해주세요.");
        }

        String pitchResult = GameLogicUtil.determinePitchResult(game.getCurrentPitcher(), "strike"); // 일단 스트라이크라고 가정
        String hitResult = GameLogicUtil.determineHitResult(swing, game.getCurrentPitcher(), pitchResult);

        logger.info("게임 {}: 타자 {} 스윙. 투수 {} 투구 결과: {}, 타격 결과: {}",
                gameId, game.getCurrentBatter().getName(), game.getCurrentPitcher().getName(), pitchResult, hitResult);


        // 결과에 따른 게임 상태 업데이트
        switch (hitResult) {
            case "스트라이크":
                game.setStrike(game.getStrike() + 1);
                break;
            case "볼":
                game.setBall(game.getBall() + 1);
                break;
            case "파울":
                if (game.getStrike() < 2) { // 2스트라이크 이후 파울은 스트라이크로 계산하지 않음
                    game.setStrike(game.getStrike() + 1);
                }
                break;
            case "안타":
                handleScore(game, 1); // 1점 추가 로직 (임시)
                GameLogicUtil.addRunnerToBase(game, 1, game.getCurrentBatter()); // 타자 1루 진루
                GameLogicUtil.advanceRunners(game, 1); // 모든 주자 1칸 진루
                game.setStrike(0);
                game.setBall(0);
                advanceBattingOrder(game); // 다음 타자로 변경
                break;
            case "홈런!":
                handleScore(game, 4); // 4점 추가 로직 (임시)
                game.getCurrentBatter().setHomeRuns(game.getCurrentBatter().getHomeRuns() + 1);
                game.getCurrentBatter().setRbis(game.getCurrentBatter().getRbis() + (game.getBaseRunners().size() + 1));
                GameLogicUtil.resetBases(game);
                game.setStrike(0);
                game.setBall(0);
                advanceBattingOrder(game);
                break;
            case "뜬공 아웃":
                game.setOut(game.getOut() + 1);
                game.setStrike(0);
                game.setBall(0);
                advanceBattingOrder(game);
                break;
            case "삼진 아웃":
                game.setOut(game.getOut() + 1);
                game.setStrike(0);
                game.setBall(0);
                advanceBattingOrder(game);
                break;
            case "땅볼 아웃": // 땅볼 로직은 GameLogicUtil 내에서 처리되므로 여기서는 단순히 아웃만 반영
                game.setOut(game.getOut() + 1);
                game.setStrike(0);
                game.setBall(0);
                advanceBattingOrder(game);
                break;
            case "병살타!": // 병살타 로직도 GameLogicUtil 내에서 처리되므로 여기서는 아웃만 반영
                game.setOut(game.getOut() + 2); // GameLogicUtil에서 이미 아웃 처리했지만, 여기도 명시
                game.setStrike(0);
                game.setBall(0);
                GameLogicUtil.resetBases(game); // 병살타시 모든 주자 귀루 또는 아웃
                advanceBattingOrder(game);
                break;
        }

        checkCount(game); // 스트라이크, 볼, 아웃 카운트 확인 및 처리
        checkGameOver(game); // 게임 종료 여부 확인

        // gameRepository.save(game);
        return hitResult;
    }

    @Override
    @Transactional
    public String pitcherThrow(String gameId, String pitchType) {
        GameDto game = getGame(gameId);

        if (game.isGameOver()) {
            throw new InvalidGameStateException("게임이 이미 종료되었습니다.");
        }
        if (game.getCurrentBatter() == null || game.getCurrentPitcher() == null) {
            throw new InvalidGameStateException("현재 타자 또는 투수가 설정되지 않았습니다. 라인업을 먼저 설정해주세요.");
        }
        if (game.getOut() >= 3 && game.getStrike() == 0 && game.getBall() == 0) {
            throw new InvalidGameStateException("이미 3아웃입니다. 다음 이닝으로 진행해주세요.");
        }

        // 스윙 없이 투구 결과만 계산
        String pitchResult = GameLogicUtil.determinePitchResult(game.getCurrentPitcher(), pitchType);

        logger.info("게임 {}: 투수 {} 투구 ({}). 결과: {}",
                gameId, game.getCurrentPitcher().getName(), pitchType, pitchResult);

        switch (pitchResult) {
            case "스트라이크":
                game.setStrike(game.getStrike() + 1);
                break;
            case "볼":
                game.setBall(game.getBall() + 1);
                break;
        }

        checkCount(game); // 스트라이크, 볼, 아웃 카운트 확인 및 처리
        checkGameOver(game); // 게임 종료 여부 확인

        // gameRepository.save(game);
        return pitchResult;
    }

    @Override
    @Transactional
    public GameDto nextInning(String gameId) {
        GameDto game = getGame(gameId);

        // 현재 이닝의 말 공격이 끝났다면 다음 이닝으로, 아니면 공수 교대
        if (game.getOut() < 3) { // 3아웃이 안됐는데 다음 이닝 요청시
            throw new InvalidGameStateException("아직 현재 이닝이 끝나지 않았습니다 (3아웃이 아닙니다).");
        }

        if (game.isTop()) { // 현재 이닝 초였으면 -> 말로
            game.setTop(false);
            game.setOut(0);
            game.setStrike(0);
            game.setBall(0);
            GameLogicUtil.resetBases(game);
            // 원정팀 투수 -> 홈팀 타자
            game.setCurrentPitcher(game.getAwayStartingPitcher()); // 또는 현재 등판 중인 투수
            game.setBattingOrder(game.getHomeTeamBattingOrder()); // 홈팀 타순으로 변경
            game.setCurrentBatterIndex(0);
            game.setCurrentBatter(game.getHomeTeamBattingOrder().get(game.getCurrentBatterIndex()));
            logger.info("게임 {}: {}회 말로 진행. 현재 타자: {}", gameId, game.getInning(), game.getCurrentBatter().getName());
        } else { // 현재 이닝 말이었으면 -> 다음 이닝 초로
            game.setInning(game.getInning() + 1);
            game.setTop(true);
            game.setOut(0);
            game.setStrike(0);
            game.setBall(0);
            GameLogicUtil.resetBases(game);
            // 홈팀 투수 -> 원정팀 타자
            game.setCurrentPitcher(game.getHomeStartingPitcher()); // 또는 현재 등판 중인 투수
            game.setBattingOrder(game.getAwayTeamBattingOrder()); // 원정팀 타순으로 변경
            game.setCurrentBatterIndex(0);
            game.setCurrentBatter(game.getAwayTeamBattingOrder().get(game.getCurrentBatterIndex()));
            logger.info("게임 {}: {}회 초로 진행. 현재 타자: {}", gameId, game.getInning(), game.getCurrentBatter().getName());
        }

        checkGameOver(game); // 게임 종료 여부 다시 확인

        // gameRepository.save(game);
        return game;
    }

    @Override
    @Transactional
    public GameDto endGame(String gameId) {
        GameDto game = getGame(gameId);
        game.setGameOver(true);
        // 승자 결정 로직 (점수 비교 등)
        if (game.getHomeScore() > game.getAwayScore()) {
            game.setWinner(game.getHomeTeam());
        } else if (game.getAwayScore() > game.getHomeScore()) {
            game.setWinner(game.getAwayTeam());
        } else {
            game.setWinner("무승부"); // 또는 연장전 처리
        }
        logger.info("게임 {} 종료. 승자: {}", gameId, game.getWinner());
        // gameRepository.save(game);
        return game;
    }

    @Override
    @Transactional
    public void advanceRunners(String gameId, Integer basesToAdvance) {
        GameDto game = getGame(gameId);
        if (game.isGameOver()) {
            throw new InvalidGameStateException("게임이 이미 종료되었습니다.");
        }
        GameLogicUtil.advanceRunners(game, basesToAdvance);
        // gameRepository.save(game);
        logger.info("게임 {}: 주자들이 {} 베이스 진루했습니다.", gameId, basesToAdvance);
    }

    @Override
    public String getGameStats(String gameId) {
        GameDto game = getGame(gameId);
        StringBuilder stats = new StringBuilder();
        stats.append(String.format("게임 ID: %s\n", game.getGameId()));
        stats.append(String.format("이닝: %d회 %s\n", game.getInning(), game.isTop() ? "초" : "말"));
        stats.append(String.format("현재 점수: %s %d : %d %s\n", game.getAwayTeam(), game.getAwayScore(), game.getHomeScore(), game.getHomeTeam()));
        stats.append(String.format("아웃: %d, 스트라이크: %d, 볼: %d\n", game.getOut(), game.getStrike(), game.getBall()));
        stats.append("루상 주자: ");
        if (game.getBases()[1] != null) stats.append("1루: ").append(game.getBases()[1].getName()).append(" ");
        if (game.getBases()[2] != null) stats.append("2루: ").append(game.getBases()[2].getName()).append(" ");
        if (game.getBases()[3] != null) stats.append("3루: ").append(game.getBases()[3].getName()).append(" ");
        if (game.getBases()[1] == null && game.getBases()[2] == null && game.getBases()[3] == null) stats.append("없음");
        stats.append("\n");

        if (game.getCurrentBatter() != null) {
            stats.append(String.format("현재 타자: %s (타율: %.3f)\n", game.getCurrentBatter().getName(), game.getCurrentBatter().getBattingAverage()));
        }
        if (game.getCurrentPitcher() != null) {
            stats.append(String.format("현재 투수: %s (ERA: %.2f)\n", game.getCurrentPitcher().getName(), game.getCurrentPitcher().getEra()));
        }
        if (game.isGameOver()) {
            stats.append(String.format("게임 종료! 승자: %s\n", game.getWinner()));
        }
        return stats.toString();
    }

    @Override
    @Transactional
    public void setTeamLineupAndPitcher(String gameId, String teamName, List<String> battingOrderPlayerNames, String startingPitcherName) {
        GameDto game = getGame(gameId);

        // 타자 정보 로드 및 설정
        List<Batter> batters = batterMapper.findByNames(battingOrderPlayerNames);
        if (batters.size() != battingOrderPlayerNames.size()) {
            throw new ValidationException("라인업에 포함된 일부 타자 이름을 찾을 수 없습니다. 모든 타자 이름이 유효한지 확인해주세요.");
        }
        // 로드된 타자들이 모두 해당 팀 소속인지 확인
        for (Batter batter : batters) {
            if (!batter.getTeam().equals(teamName)) {
                throw new ValidationException("타자 '" + batter.getName() + "'는 팀 '" + teamName + "' 소속이 아닙니다.");
            }
        }
        // 타순 설정
        if (game.getHomeTeam().equals(teamName)) {
            game.setHomeTeamBattingOrder(batters);
            logger.info("게임 {}: 홈팀 타순 설정 완료.", gameId);
        } else if (game.getAwayTeam().equals(teamName)) {
            game.setAwayTeamBattingOrder(batters);
            logger.info("게임 {}: 원정팀 타순 설정 완료.", gameId);
        } else {
            throw new ValidationException("유효하지 않은 팀 이름입니다: " + teamName);
        }

        // 투수 정보 로드 및 설정
        Pitcher pitcher = pitcherMapper.findByName(startingPitcherName);
        if (pitcher == null || !pitcher.getTeam().equals(teamName)) {
            throw new ValidationException("팀 " + teamName + "에서 선발 투수 '" + startingPitcherName + "'를 찾을 수 없거나 해당 팀 소속이 아닙니다.");
        }

        // 해당 팀의 startingPitcher 필드에 설정
        if (game.getHomeTeam().equals(teamName)) {
            game.setHomeStartingPitcher(pitcher);
            logger.info("게임 {}: 홈팀 선발 투수 설정 완료: {}", gameId, pitcher.getName());
        } else if (game.getAwayTeam().equals(teamName)) {
            game.setAwayStartingPitcher(pitcher);
            logger.info("게임 {}: 원정팀 선발 투수 설정 완료: {}", gameId, pitcher.getName());
        }

        // 게임의 현재 타자/투수 초기 설정 (게임 시작 시 한 번 호출)
        if (game.getCurrentBatter() == null && game.getCurrentPitcher() == null) {
            if (game.isIsUserOffense()) { // 사용자가 공격팀이면
                if (game.isTop()) { // 초 공격 (원정팀 공격)
                    game.setCurrentPitcher(game.getHomeStartingPitcher()); // 홈팀 투수
                    game.setBattingOrder(game.getAwayTeamBattingOrder()); // 원정팀 타순
                } else { // 말 공격 (홈팀 공격)
                    game.setCurrentPitcher(game.getAwayStartingPitcher()); // 원정팀 투수
                    game.setBattingOrder(game.getHomeTeamBattingOrder()); // 홈팀 타순
                }
            } else { // 사용자가 수비팀이면
                if (game.isTop()) { // 초 공격 (원정팀 공격)
                    game.setCurrentPitcher(game.getHomeStartingPitcher()); // 홈팀 투수
                    game.setBattingOrder(game.getAwayTeamBattingOrder()); // 원정팀 타순
                } else { // 말 공격 (홈팀 공격)
                    game.setCurrentPitcher(game.getAwayStartingPitcher()); // 원정팀 투수
                    game.setBattingOrder(game.getHomeTeamBattingOrder()); // 홈팀 타순
                }
            }
            if (!game.getBattingOrder().isEmpty()) {
                game.setCurrentBatter(game.getBattingOrder().get(0));
                game.setCurrentBatterIndex(0);
            }
            logger.info("게임 {}: 초기 타자/투수 설정 완료. 현재 타자: {}, 현재 투수: {}",
                    gameId, game.getCurrentBatter() != null ? game.getCurrentBatter().getName() : "없음",
                    game.getCurrentPitcher() != null ? game.getCurrentPitcher().getName() : "없음");
        }
        games.put(gameId, game); // 메모리 내 게임 상태 업데이트
    }

    @Override
    public void setComputerLineupAndPitcher(String gameId, String teamName, List<String> battingOrderPlayerNames, String startingPitcherName) {
        // 이 메서드는 setTeamLineupAndPitcher와 동일한 로직을 사용합니다.
        // 컴퓨터 팀의 라인업 설정도 동일한 유효성 검사 및 설정 로직을 따릅니다.
        setTeamLineupAndPitcher(gameId, teamName, battingOrderPlayerNames, startingPitcherName);
    }

    protected void checkCount(GameDto game) { // private -> protected 변경
        if (game.getStrike() >= 3) {
            game.setOut(game.getOut() + 1);
            game.setStrike(0);
            game.setBall(0);
            // 3스트라이크 아웃 시 타자 변경
            advanceBattingOrder(game);
        }
        if (game.getBall() >= 4) {
            game.setBall(0);
            game.setStrike(0);
            if (game.getCurrentBatter() != null) {
                GameLogicUtil.addRunnerToBase(game, 1, game.getCurrentBatter());
            }
            // 4볼 볼넷 시 타자 변경
            GameLogicUtil.advanceRunners(game, 1); // 볼넷 시 1베이스 진루 (타자 포함)
            advanceBattingOrder(game);
        }
        if (game.getOut() >= 3) {
            // 3아웃 시 이닝 종료 처리 (다음 이닝 또는 공수 교대)
            // 다음 이닝 처리는 nextInning 메서드에서 수행하므로 여기서는 단순히 아웃 카운트만 리셋
            // nextInning(game.getGameId()); // 이 메서드 호출은 클라이언트 요청에 따라 이루어져야 함
            logger.info("게임 {}: 3아웃, 이닝 종료 준비. 점수: {}:{} vs {}:{}",
                    game.getGameId(), game.getAwayTeam(), game.getAwayScore(), game.getHomeTeam(), game.getHomeScore());
        }
    }

    protected void advanceBattingOrder(GameDto game) {
        if (game.getBattingOrder() == null || game.getBattingOrder().isEmpty()) {
            logger.warn("게임 {}: 타순이 설정되지 않았습니다.", game.getGameId());
            return;
        }
        int nextIndex = (game.getCurrentBatterIndex() + 1) % game.getBattingOrder().size();
        game.setCurrentBatterIndex(nextIndex);
        game.setCurrentBatter(game.getBattingOrder().get(nextIndex));
        logger.info("게임 {}: 다음 타자: {} (타순 {})", game.getGameId(), game.getCurrentBatter().getName(), nextIndex + 1);
    }

    protected void checkGameOver(GameDto game) {
        // 9회 말까지 끝났고 점수 차이가 나면 게임 종료
        // 또는 연장전 규칙 추가
        if (game.getInning() >= game.getMaxInning() && !game.isTop() && game.getOut() >= 3) {
            if (game.getHomeScore() != game.getAwayScore()) {
                endGame(game.getGameId());
            } else {
                // 동점이면 연장전 규칙 (현재는 무제한 연장으로 가정하거나 추가 규칙 필요)
                // 여기서는 일단 다음 이닝으로 넘어가게 합니다. 실제 야구 로직에 따라 수정 필요.
                logger.info("게임 {}: {}회 말 종료 동점. 연장전 또는 다음 이닝으로 진행.", game.getGameId(), game.getMaxInning());
            }
        }
    }

    protected void handleScore(GameDto game, int score) {
        if (game.isTop()) { // 초 공격 (원정팀 공격)
            game.setAwayScore(game.getAwayScore() + score);
        } else { // 말 공격 (홈팀 공격)
            game.setHomeScore(game.getHomeScore() + score);
        }
        logger.info("게임 {}: 점수 발생! 현재 점수: {}:{} vs {}:{}",
                game.getGameId(), game.getAwayTeam(), game.getAwayScore(), game.getHomeTeam(), game.getHomeScore());
    }
}