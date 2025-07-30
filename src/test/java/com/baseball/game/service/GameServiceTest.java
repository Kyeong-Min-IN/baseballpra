// src/test/java/com/baseball/game/service/GameServiceTest.java
// 이 파일은 com.baseball.game.service 패키지에 위치해야 합니다.
package com.baseball.game.service;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.GameDto;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.exception.GameNotFoundException;
import com.baseball.game.exception.InvalidGameStateException;
import com.baseball.game.exception.ValidationException;
import com.baseball.game.repository.GameRepository;
import com.baseball.game.util.GameLogicUtil; // GameLogicUtil import
import com.baseball.game.util.ValidationUtil; // ValidationUtil import

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy; // Spy 추가

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GameServiceTest {

    @InjectMocks
    @Spy // GameServiceImpl의 실제 메서드를 호출하면서 일부 메서드를 Mocking하기 위해 Spy 사용
    private GameServiceImpl gameService; // 테스트할 서비스 구현체

    @Mock
    private GameRepository gameRepository; // 서비스가 의존하는 리포지토리 (목 객체)

    // GameServiceImpl 내부의 'games' 맵에 접근하기 위한 리플렉션
    // 실제 환경에서는 Mockito를 통해 리포지토리를 모킹하여 데이터 접근을 제어합니다.
    private Map<String, GameDto> games;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화

        // GameServiceImpl의 private 필드인 'games' 맵에 접근하여 초기화
        // 이는 GameServiceImpl이 임시로 메모리 맵을 사용하는 방식에 대한 테스트를 위함입니다.
        // 실제 Redis 연동 시에는 이 부분이 필요 없거나, Redis Mocking 라이브러리를 사용해야 합니다.
        java.lang.reflect.Field gamesField = GameServiceImpl.class.getDeclaredField("games");
        gamesField.setAccessible(true); // private 필드에 접근 허용
        games = (Map<String, GameDto>) gamesField.get(gameService);
        games.clear(); // 각 테스트 전에 맵 초기화
    }

    // --- 게임 생성 (createGame) 테스트 ---

    /**
     * 게임 생성 성공 테스트
     * 유효한 홈팀과 원정팀 이름으로 게임이 성공적으로 생성되는지 확인합니다.
     */
    @Test
    void createGame_성공() {
        String homeTeam = "HomeTeam";
        String awayTeam = "AwayTeam";

        GameDto createdGame = gameService.createGame(homeTeam, awayTeam);

        assertNotNull(createdGame);
        assertNotNull(createdGame.getGameId());
        assertEquals(homeTeam, createdGame.getHomeTeam());
        assertEquals(awayTeam, createdGame.getAwayTeam());
        assertTrue(games.containsKey(createdGame.getGameId())); // 메모리 맵에 저장되었는지 확인
        assertEquals(createdGame, games.get(createdGame.getGameId()));
    }

    /**
     * 게임 생성 실패 테스트: 홈팀 이름이 null일 경우
     * ValidationException이 발생하는지 확인합니다.
     */
    @Test
    void createGame_홈팀_null_예외() {
        String homeTeam = null;
        String awayTeam = "AwayTeam";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            gameService.createGame(homeTeam, awayTeam);
        });
        assertEquals("홈팀과 원정팀 모두 필수입니다.", exception.getMessage());
    }

    /**
     * 게임 생성 실패 테스트: 원정팀 이름이 null일 경우
     * ValidationException이 발생하는지 확인합니다.
     */
    @Test
    void createGame_원정팀_null_예외() {
        String homeTeam = "HomeTeam";
        String awayTeam = null;

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            gameService.createGame(homeTeam, awayTeam);
        });
        assertEquals("홈팀과 원정팀 모두 필수입니다.", exception.getMessage());
    }

    /**
     * 게임 생성 실패 테스트: 홈팀과 원정팀 이름이 동일할 경우
     * ValidationException이 발생하는지 확인합니다.
     */
    @Test
    void createGame_동일팀_예외() {
        String homeTeam = "SameTeam";
        String awayTeam = "SameTeam";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            gameService.createGame(homeTeam, awayTeam);
        });
        assertEquals("홈팀과 원정팀은 서로 다른 팀이어야 합니다.", exception.getMessage());
    }

    // --- 게임 조회 (getGame) 테스트 ---

    /**
     * 게임 조회 성공 테스트
     * 유효한 게임 ID로 게임 정보가 성공적으로 조회되는지 확인합니다.
     */
    @Test
    void getGame_성공() {
        GameDto game = new GameDto();
        game.setGameId("testGameId");
        games.put(game.getGameId(), game); // 메모리 맵에 게임 추가

        GameDto foundGame = gameService.getGame("testGameId");

        assertNotNull(foundGame);
        assertEquals("testGameId", foundGame.getGameId());
    }

    /**
     * 게임 조회 실패 테스트: 존재하지 않는 게임 ID일 경우
     * GameNotFoundException이 발생하는지 확인합니다.
     */
    @Test
    void getGame_없는게임_예외() {
        GameNotFoundException exception = assertThrows(GameNotFoundException.class, () -> {
            gameService.getGame("nonExistentId");
        });
        assertTrue(exception.getMessage().contains("게임을 찾을 수 없습니다. GameId: nonExistentId"));
    }

    /**
     * 게임 조회 실패 테스트: 게임 ID가 null일 경우
     * ValidationException이 발생하는지 확인합니다.
     */
    @Test
    void getGame_ID_null_예외() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            gameService.getGame(null);
        });
        assertEquals("게임 ID는 필수입니다.", exception.getMessage());
    }

    /**
     * 게임 조회 실패 테스트: 게임 ID가 비어있을 경우
     * ValidationException이 발생하는지 확인합니다.
     */
    @Test
    void getGame_ID_빈값_예외() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            gameService.getGame(" ");
        });
        assertEquals("게임 ID는 필수입니다.", exception.getMessage());
    }

    // --- 타격 (batterSwing) 테스트 ---

    /**
     * 타격 성공 테스트: 안타
     * 타격 후 게임 상태(점수, 베이스 주자)가 올바르게 업데이트되는지 확인합니다.
     */
    @Test
    void batterSwing_성공_안타() {
        GameDto game = createTestGame("swingTestId");
        game.setIsUserOffense(true); // 유저 공격 턴으로 설정
        Batter currentBatter = new Batter(); // 현재 타자 설정
        currentBatter.setName("테스트타자");
        currentBatter.setContact(70); // 컨택트 능력치 설정
        game.setCurrentBatter(currentBatter);

        // GameLogicUtil.determineHitResultWithTiming 모킹하여 "안타" 반환하도록 설정
        try (var mockedStatic = mockStatic(GameLogicUtil.class)) {
            mockedStatic.when(() -> GameLogicUtil.determineHitResultWithTiming(anyBoolean(), any(), anyString(), anyDouble(), any())).thenReturn("안타");
            // GameLogicUtil.addRunnerToBase 및 advanceRunners의 실제 동작을 시뮬레이션
            mockedStatic.when(() -> GameLogicUtil.addRunnerToBase(any(), anyInt(), any())).thenAnswer(invocation -> {
                GameDto g = invocation.getArgument(0);
                int base = invocation.getArgument(1);
                Batter runner = invocation.getArgument(2);
                g.getBases()[base] = runner;
                if (base > 0) { // 1루, 2루, 3루 주자 리스트에 추가
                    g.getBaseRunners().add(runner);
                }
                return null;
            });
            mockedStatic.when(() -> GameLogicUtil.advanceRunners(any(), anyInt())).thenAnswer(invocation -> {
                GameDto g = invocation.getArgument(0);
                int basesToAdvance = invocation.getArgument(1);
                Batter[] currentBases = g.getBases();
                Batter[] newBases = new Batter[4]; // 새로운 베이스 상태 배열

                for (int i = 3; i >= 1; i--) { // 3루부터 1루까지 순회 (홈은 0)
                    if (currentBases[i] != null) {
                        Batter runner = currentBases[i];
                        int newBasePosition = i + basesToAdvance;

                        if (newBasePosition >= 4) { // 주자가 3루를 넘어 홈으로 들어옴 (득점)
                            g.setHomeScore(g.getHomeScore() + 1); // 홈팀 점수 증가
                        } else {
                            newBases[newBasePosition] = runner; // 새로운 베이스에 주자 배치
                        }
                    }
                }
                g.setBases(newBases); // 게임의 베이스 상태 업데이트
                g.getBaseRunners().clear(); // 기존 주자 리스트 초기화
                for (int i = 1; i <= 3; i++) { // 새로운 주자 리스트 구성
                    if (newBases[i] != null) {
                        g.getBaseRunners().add(newBases[i]);
                    }
                }
                return null;
            });


            String result = gameService.batterSwing("swingTestId", true, 0.5);

            assertEquals("안타", result);
            assertEquals(0, game.getStrike()); // 안타 후 스트라이크 초기화
            assertEquals(0, game.getBall());   // 안타 후 볼 초기화
            
            // 안타 후 1루에 있던 타자가 1베이스 진루하여 2루로 이동했는지 확인
            assertNull(game.getBases()[1]); // 1루는 비어있어야 함
            assertNotNull(game.getBases()[2]); // 2루에 주자가 있어야 함
            assertEquals(currentBatter, game.getBases()[2]); // 현재 타자가 2루로 이동했는지 확인
        }
    }

    /**
     * 타격 실패 테스트: 게임 종료 상태
     * 게임이 이미 종료되었을 때 InvalidGameStateException이 발생하는지 확인합니다.
     */
    @Test
    void batterSwing_게임종료_예외() {
        GameDto game = createTestGame("gameOverSwingId");
        game.setGameOver(true); // 게임 종료 상태로 설정

        InvalidGameStateException exception = assertThrows(InvalidGameStateException.class, () -> {
            gameService.batterSwing("gameOverSwingId", true, 0.5);
        });
        assertEquals("이미 종료된 게임입니다.", exception.getMessage());
    }

    /**
     * 타격 실패 테스트: 유저 공격 턴이 아닐 때
     * 현재 턴이 유저 공격 턴이 아닐 때 InvalidGameStateException이 발생하는지 확인합니다.
     */
    @Test
    void batterSwing_유저공격턴아님_예외() {
        GameDto game = createTestGame("notUserOffenseId");
        game.setIsUserOffense(false); // 유저 공격 턴 아님으로 설정

        InvalidGameStateException exception = assertThrows(InvalidGameStateException.class, () -> {
            gameService.batterSwing("notUserOffenseId", true, 0.5);
        });
        assertEquals("현재는 투수 턴입니다.", exception.getMessage());
    }

    // --- 투구 (pitcherThrow) 테스트 ---

    /**
     * 투구 성공 테스트: 스트라이크
     * 투구 후 게임 상태(볼/스트라이크 카운트)가 올바르게 업데이트되는지 확인합니다.
     */
    @Test
    void pitcherThrow_성공_스트라이크() {
        GameDto game = createTestGame("pitchTestId");
        game.setIsUserOffense(false); // 유저 수비 턴 (투수 턴)으로 설정
        game.setStrike(0);
        game.setBall(0);
        
        // currentPitcher가 null이 아님을 확인
        assertNotNull(game.getCurrentPitcher(), "currentPitcher는 null이 아니어야 합니다.");

        try (var mockedStatic = mockStatic(GameLogicUtil.class)) {
            // GameLogicUtil.determineHitResult 모킹하여 "스트라이크" 반환하도록 설정
            // 이때, any(Pitcher.class)를 사용하여 Mocking 설정 시점에 null 체크를 우회
            mockedStatic.when(() -> GameLogicUtil.determineHitResult(any(Boolean.class), any(Pitcher.class), anyString())).thenReturn("스트라이크");
            
            // checkCount 내부에서 nextInning 호출될 수 있으므로 Mocking
            doReturn(game).when(gameService).nextInning(anyString()); 

            // 실제 pitcherThrow 메서드 호출 (Spy이므로 실제 구현이 실행됨)
            String result = gameService.pitcherThrow("pitchTestId", "strike");

            assertTrue(result.contains("스트라이크"));
            
            // GameLogicUtil.determineHitResult가 호출되었는지 확인
            // 이때 any(Pitcher.class)는 null이 아닌 Pitcher 객체가 전달되었음을 의미
            mockedStatic.verify(() -> GameLogicUtil.determineHitResult(any(Boolean.class), any(Pitcher.class), eq("strike")), times(1));

            assertEquals(1, game.getStrike());
            assertEquals(0, game.getBall());
        }
    }

    /**
     * 투구 실패 테스트: 게임 종료 상태
     * 게임이 이미 종료되었을 때 InvalidGameStateException이 발생하는지 확인합니다.
     */
    @Test
    void pitcherThrow_게임종료_예외() {
        GameDto game = createTestGame("gameOverPitchId");
        game.setGameOver(true);

        InvalidGameStateException exception = assertThrows(InvalidGameStateException.class, () -> {
            gameService.pitcherThrow("gameOverPitchId", "strike");
        });
        assertEquals("이미 종료된 게임입니다.", exception.getMessage());
    }

    /**
     * 투구 실패 테스트: 투구 타입이 null일 때
     * 투구 타입이 null일 때 `IllegalArgumentException` 대신 `userAction`이 반환하는 메시지를 확인합니다.
     */
    @Test
    void pitcherThrow_투구타입_null_예외() {
        GameDto game = createTestGame("nullPitchTypeId");
        game.setIsUserOffense(false);

        // userAction이 "투구 타입을 입력하세요."를 반환하는지 확인
        String result = gameService.pitcherThrow("nullPitchTypeId", null);
        assertEquals("투구 타입을 입력하세요.", result);
    }

    // --- 다음 이닝 (nextInning) 테스트 ---

    /**
     * 다음 이닝 성공 테스트: 초 -> 말 전환
     * 이닝이 초에서 말로 올바르게 전환되는지 확인합니다.
     */
    @Test
    void nextInning_성공_초에서말() {
        GameDto game = createTestGame("nextInningId");
        game.setInning(1);
        game.setTop(true); // 1회 초
        game.setOut(3); // 3아웃 상태

        // nextInning 내부에서 endGame이 호출될 수 있으므로 Mocking
        doReturn(game).when(gameService).endGame(anyString());

        GameDto updatedGame = gameService.nextInning("nextInningId");

        assertNotNull(updatedGame);
        assertEquals(1, updatedGame.getInning()); // 이닝은 그대로 1회
        assertFalse(updatedGame.isTop()); // 말로 전환
        assertEquals(0, updatedGame.getOut());
        assertEquals(0, updatedGame.getStrike());
        assertEquals(0, updatedGame.getBall());
        assertFalse(updatedGame.isIsUserOffense()); // 턴 전환 (유저 → 컴퓨터)
    }

    /**
     * 다음 이닝 성공 테스트: 말 -> 다음 이닝 초 전환
     * 이닝이 말에서 다음 이닝 초로 올바르게 전환되는지 확인합니다.
     */
    @Test
    void nextInning_성공_말에서다음이닝초() {
        GameDto game = createTestGame("nextInningId2");
        game.setInning(1);
        game.setTop(false); // 1회 말
        game.setOut(3); // 3아웃 상태
        game.setIsUserOffense(false); // 1회 말은 컴퓨터 공격 턴이므로 false로 명시적 설정

        // nextInning 내부에서 endGame이 호출될 수 있으므로 Mocking
        doReturn(game).when(gameService).endGame(anyString());

        GameDto updatedGame = gameService.nextInning("nextInningId2");

        assertNotNull(updatedGame);
        assertEquals(2, updatedGame.getInning()); // 2회로 전환
        assertTrue(updatedGame.isTop()); // 초로 전환
        assertEquals(0, updatedGame.getOut());
        assertEquals(0, updatedGame.getStrike());
        assertEquals(0, updatedGame.getBall());
        // 1회 말 (isUserOffense: false) -> 2회 초 (isUserOffense: true)
        // 로직상 nextTurn에서 isUserOffense가 반전되므로 true가 되어야 함
        assertTrue(updatedGame.isIsUserOffense()); // 턴 전환 (컴퓨터 → 유저)
    }

    /**
     * 다음 이닝 실패 테스트: 게임 종료 상태
     * 게임이 이미 종료되었을 때 InvalidGameStateException이 발생하는지 확인합니다.
     */
    @Test
    void nextInning_게임종료_예외() {
        GameDto game = createTestGame("gameOverNextInningId");
        game.setGameOver(true);

        InvalidGameStateException exception = assertThrows(InvalidGameStateException.class, () -> {
            gameService.nextInning("gameOverNextInningId");
        });
        assertEquals("이미 종료된 게임입니다.", exception.getMessage());
    }

    // --- 게임 종료 (endGame) 테스트 ---

    /**
     * 게임 종료 성공 테스트: 홈팀 승리
     * 홈팀 점수가 더 높을 때 홈팀이 승자로 설정되는지 확인합니다.
     */
    @Test
    void endGame_성공_홈팀승리() {
        GameDto game = createTestGame("endGameId");
        game.setHomeScore(5);
        game.setAwayScore(3);
        game.setGameOver(false); // 게임이 아직 종료되지 않은 상태

        GameDto endedGame = gameService.endGame("endGameId");

        assertNotNull(endedGame);
        assertTrue(endedGame.isGameOver());
        assertEquals(game.getHomeTeam(), endedGame.getWinner());
    }

    /**
     * 게임 종료 성공 테스트: 원정팀 승리
     * 원정팀 점수가 더 높을 때 원정팀이 승자로 설정되는지 확인합니다.
     */
    @Test
    void endGame_성공_원정팀승리() {
        GameDto game = createTestGame("endGameId2");
        game.setHomeScore(3);
        game.setAwayScore(5);
        game.setGameOver(false);

        GameDto endedGame = gameService.endGame("endGameId2");

        assertNotNull(endedGame);
        assertTrue(endedGame.isGameOver());
        assertEquals(game.getAwayTeam(), endedGame.getWinner());
    }

    /**
     * 게임 종료 성공 테스트: 무승부
     * 점수가 같을 때 무승부로 설정되는지 확인합니다.
     */
    @Test
    void endGame_성공_무승부() {
        GameDto game = createTestGame("endGameId3");
        game.setHomeScore(3);
        game.setAwayScore(3);
        game.setGameOver(false);

        GameDto endedGame = gameService.endGame("endGameId3");

        assertNotNull(endedGame);
        assertTrue(endedGame.isGameOver());
        assertEquals("무승부", endedGame.getWinner());
    }

    /**
     * 게임 종료 실패 테스트: 이미 종료된 게임
     * 게임이 이미 종료되었을 때 InvalidGameStateException이 발생하는지 확인합니다.
     */
    @Test
    void endGame_게임종료_예외() {
        GameDto game = createTestGame("alreadyEndedGameId");
        game.setGameOver(true); // 이미 종료된 상태

        InvalidGameStateException exception = assertThrows(InvalidGameStateException.class, () -> {
            gameService.endGame("alreadyEndedGameId");
        });
        assertEquals("이미 종료된 게임입니다.", exception.getMessage());
    }

    // --- 주자 진루 (advanceRunners) 테스트 ---

    /**
     * 주자 진루 성공 테스트
     * 베이스에 주자가 있을 때 올바르게 진루하고 점수가 올라가는지 확인합니다.
     */
    @Test
    void advanceRunners_성공() {
        GameDto game = createTestGame("advanceRunnersId");
        game.setHomeScore(0);
        game.setAwayScore(0);
        game.setGameOver(false);

        // 1루에 주자 설정
        Batter runnerOnFirst = new Batter();
        runnerOnFirst.setName("1루주자");
        game.getBases()[1] = runnerOnFirst;
        game.getBaseRunners().add(runnerOnFirst);

        // GameLogicUtil.advanceRunners 모킹
        try (var mockedStatic = mockStatic(GameLogicUtil.class)) {
            mockedStatic.when(() -> GameLogicUtil.advanceRunners(any(), anyInt())).thenAnswer(invocation -> {
                GameDto g = invocation.getArgument(0);
                int basesToAdvance = invocation.getArgument(1);
                Batter[] currentBases = g.getBases();
                Batter[] newBases = new Batter[4]; // 새로운 베이스 상태 배열

                for (int i = 3; i >= 1; i--) { // 3루부터 1루까지 순회 (홈은 0)
                    if (currentBases[i] != null) {
                        Batter runner = currentBases[i];
                        int newBasePosition = i + basesToAdvance;

                        if (newBasePosition >= 4) { // 주자가 3루를 넘어 홈으로 들어옴 (득점)
                            g.setHomeScore(g.getHomeScore() + 1); // 홈팀 점수 증가
                        } else {
                            newBases[newBasePosition] = runner; // 새로운 베이스에 주자 배치
                        }
                    }
                }
                g.setBases(newBases); // 게임의 베이스 상태 업데이트
                g.getBaseRunners().clear(); // 기존 주자 리스트 초기화
                for (int i = 1; i <= 3; i++) { // 새로운 주자 리스트 구성
                    if (newBases[i] != null) {
                        g.getBaseRunners().add(newBases[i]);
                    }
                }
                return null;
            });

            // 2베이스 진루 시도 (1루 주자가 3루로)
            gameService.advanceRunners("advanceRunnersId", 2);

            assertNull(game.getBases()[1]); // 1루는 비어있어야 함
            assertNotNull(game.getBases()[3]); // 3루에 주자가 있어야 함
            assertEquals(runnerOnFirst, game.getBases()[3]); // 1루 주자가 3루로 이동했는지 확인
            assertEquals(0, game.getHomeScore()); // 아직 홈으로 들어오지 않았으므로 점수 변화 없음
        }
    }

    /**
     * 주자 진루 성공 테스트: 득점 발생
     * 주자가 홈으로 들어왔을 때 점수가 올바르게 올라가는지 확인합니다.
     */
    @Test
    void advanceRunners_성공_득점() {
        GameDto game = createTestGame("advanceRunnersScoreId");
        game.setHomeScore(0);
        game.setAwayScore(0);
        game.setGameOver(false);

        // 3루에 주자 설정
        Batter runnerOnThird = new Batter();
        runnerOnThird.setName("3루주자");
        game.getBases()[3] = runnerOnThird;
        game.getBaseRunners().add(runnerOnThird);

        // GameLogicUtil.advanceRunners 모킹
        try (var mockedStatic = mockStatic(GameLogicUtil.class)) {
            mockedStatic.when(() -> GameLogicUtil.advanceRunners(any(), anyInt())).thenAnswer(invocation -> {
                GameDto g = invocation.getArgument(0);
                int basesToAdvance = invocation.getArgument(1);
                Batter[] currentBases = g.getBases();
                Batter[] newBases = new Batter[4]; // 새로운 베이스 상태 배열

                for (int i = 3; i >= 1; i--) { // 3루부터 1루까지 순회 (홈은 0)
                    if (currentBases[i] != null) {
                        Batter runner = currentBases[i];
                        int newBasePosition = i + basesToAdvance;

                        if (newBasePosition >= 4) { // 주자가 3루를 넘어 홈으로 들어옴 (득점)
                            g.setHomeScore(g.getHomeScore() + 1); // 홈팀 점수 증가
                        } else {
                            newBases[newBasePosition] = runner; // 새로운 베이스에 주자 배치
                        }
                    }
                }
                g.setBases(newBases); // 게임의 베이스 상태 업데이트
                g.getBaseRunners().clear(); // 기존 주자 리스트 초기화
                for (int i = 1; i <= 3; i++) { // 새로운 주자 리스트 구성
                    if (newBases[i] != null) {
                        g.getBaseRunners().add(newBases[i]);
                    }
                }
                return null;
            });

            // 1베이스 진루 시도 (3루 주자가 홈으로)
            gameService.advanceRunners("advanceRunnersScoreId", 1);

            assertNull(game.getBases()[3]); // 3루는 비어있어야 함 (득점했으므로)
            assertEquals(1, game.getHomeScore()); // 홈으로 들어왔으므로 1점 획득
        }
    }

    /**
     * 주자 진루 실패 테스트: 게임 종료 상태
     * 게임이 이미 종료되었을 때 InvalidGameStateException이 발생하는지 확인합니다.
     */
    @Test
    void advanceRunners_게임종료_예외() {
        GameDto game = createTestGame("gameOverAdvanceId");
        game.setGameOver(true);

        InvalidGameStateException exception = assertThrows(InvalidGameStateException.class, () -> {
            gameService.advanceRunners("gameOverAdvanceId", 1);
        });
        assertEquals("이미 종료된 게임입니다.", exception.getMessage());
    }

    // --- 게임 통계 조회 (getGameStats) 테스트 ---

    /**
     * 게임 통계 조회 성공 테스트
     * 게임 통계 문자열이 올바르게 생성되는지 확인합니다.
     */
    @Test
    void getGameStats_성공() {
        GameDto game = createTestGame("statsTestId");
        game.setInning(3);
        game.setTop(true);
        game.setOut(1);
        game.setStrike(2);
        game.setBall(3);
        game.setHomeScore(2);
        game.setAwayScore(1);
        game.setIsUserOffense(true);

        String stats = gameService.getGameStats("statsTestId");

        assertNotNull(stats);
        assertTrue(stats.contains("=== 게임 통계 ==="));
        assertTrue(stats.contains("홈팀: HomeTeam (2점)"));
        assertTrue(stats.contains("원정팀: AwayTeam (1점)"));
        assertTrue(stats.contains("이닝: 3초"));
        assertTrue(stats.contains("아웃: 1, 스트라이크: 2, 볼: 3"));
        assertTrue(stats.contains("현재 턴: 유저(타자)"));
        // createTestGame에서 타자 이름이 "타자1"로 설정되므로, 이 부분을 수정합니다.
        assertTrue(stats.contains("현재 타자: 타자1")); 
        assertTrue(stats.contains("현재 투수: 기본투수")); // createTestGame에서 설정된 기본 투수 이름
    }

    /**
     * 게임 통계 조회 실패 테스트: 없는 게임 ID
     * 존재하지 않는 게임 ID로 통계 조회 시 GameNotFoundException이 발생하는지 확인합니다.
     */
    @Test
    void getGameStats_없는게임_예외() {
        GameNotFoundException exception = assertThrows(GameNotFoundException.class, () -> {
            gameService.getGameStats("nonExistentStatsId");
        });
        assertTrue(exception.getMessage().contains("게임을 찾을 수 없습니다. GameId: nonExistentStatsId"));
    }

    // --- 헬퍼 메서드 ---

    /**
     * 테스트를 위한 GameDto 객체를 생성하고 games 맵에 추가하는 헬퍼 메서드
     * @param gameId 생성할 게임의 ID
     * @return 생성된 GameDto 객체
     */
    private GameDto createTestGame(String gameId) {
        GameDto game = new GameDto();
        game.setGameId(gameId);
        game.setHomeTeam("HomeTeam");
        game.setAwayTeam("AwayTeam");
        game.setMaxInning(9); // 기본 이닝 설정
        game.setIsUserOffense(true); // 기본적으로 유저 공격 턴으로 시작

        // 타순 및 현재 타자/투수 초기화
        game.setBattingOrder(new ArrayList<>());
        for (int i = 0; i < 9; i++) { // 9명의 타자 추가
            Batter batter = new Batter();
            batter.setName("타자" + (i + 1));
            game.getBattingOrder().add(batter);
        }
        game.setCurrentBatterIndex(0); // 첫 번째 타자로 설정
        game.setCurrentBatter(game.getBattingOrder().get(game.getCurrentBatterIndex())); // 현재 타자 설정

        game.setPitcherList(new ArrayList<>());
        Pitcher pitcher = new Pitcher();
        pitcher.setName("기본투수");
        game.getPitcherList().add(pitcher);
        game.setStartingPitcher(pitcher); // 선발 투수 설정
        game.setCurrentPitcher(pitcher); // 현재 투수 설정


        games.put(gameId, game);
        return game;
    }
}
