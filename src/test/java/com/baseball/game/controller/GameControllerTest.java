// src/test/java/com/baseball/game/controller/GameControllerTest.java
// 이 파일은 com.baseball.game.controller 패키지에 위치해야 합니다.
package com.baseball.game.controller;

import com.baseball.game.dto.GameCreateRequest;
import com.baseball.game.dto.GameDto;
import com.baseball.game.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing; // doNothing을 위한 import
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// com.baseball.game.dto 패키지에 있는 Batter 클래스를 import 합니다.
import com.baseball.game.dto.Batter;


public class GameControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .addFilters((request, response, chain) -> {
                    // MockHttpServletResponse의 문자 인코딩을 UTF-8로 강제 설정
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                })
                .build();
    }

    /**
     * 게임 생성(createGame) API의 성공 케이스를 테스트합니다.
     * GameService의 createGame 메서드를 모킹하여 예상 결과를 반환하도록 설정합니다.
     */
    @Test
    public void createGame_성공() throws Exception {
        // 게임 생성 요청 객체 생성 및 필드 설정
        GameCreateRequest request = new GameCreateRequest();
        request.setHomeTeam("A팀"); // 홈팀 이름
        request.setAwayTeam("B팀"); // 원정팀 이름
        request.setMaxInning(9); // 최대 이닝 설정

        // GameService가 반환할 GameDto 객체 생성 및 설정
        GameDto gameDto = new GameDto();
        gameDto.setGameId("testId"); // 생성될 게임의 ID
        gameDto.setMaxInning(9); // 설정된 최대 이닝

        // gameService.createGame 호출 시 gameDto를 반환하도록 모킹
        when(gameService.createGame(anyString(), anyString())).thenReturn(gameDto);

        // MockMvc를 사용하여 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/baseball/game")
                .contentType(MediaType.APPLICATION_JSON) // 요청 본문 타입 설정
                .content(objectMapper.writeValueAsString(request))) // 요청 본문 JSON으로 변환
                .andExpect(status().isOk()) // HTTP 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.success").value(true)) // success 필드 추가 확인 (컨트롤러 변경에 따라)
                .andExpect(jsonPath("$.game.gameId").value("testId")); // 응답의 game.gameId 필드가 예상 값과 일치하는지 확인
    }

    /**
     * 게임 생성(createGame) API에서 유효하지 않은 팀 이름으로 요청했을 때 예외를 테스트합니다.
     * GameService의 createGame 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void createGame_유효하지않은팀이름_예외() throws Exception {
        GameCreateRequest request = new GameCreateRequest();
        request.setHomeTeam(""); // 유효하지 않은 홈팀 이름
        request.setAwayTeam("B팀");
        request.setMaxInning(9);

        // ValidationUtil.validateTeamName에서 예외가 발생하도록 모킹 (이 부분은 서비스 모킹이 아닌 컨트롤러의 직접적인 유효성 검사 로직을 따릅니다.)
        // 따라서 when().thenThrow()는 필요하지 않습니다. 컨트롤러의 try-catch가 직접 처리합니다.
        // when(gameService.createGame(anyString(), anyString())).thenThrow(new RuntimeException("팀 이름은 비어 있을 수 없습니다."));

        mockMvc.perform(post("/api/baseball/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("팀명은 필수입니다."))); // 메시지 내용이 "팀명은 필수입니다."로 변경됨
    }

    /**
     * 게임 생성(createGame) API에서 동일한 팀 이름으로 요청했을 때 예외를 테스트합니다.
     * GameService의 createGame 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void createGame_동일팀이름_예외() throws Exception {
        GameCreateRequest request = new GameCreateRequest();
        request.setHomeTeam("A팀");
        request.setAwayTeam("A팀"); // 동일한 원정팀 이름
        request.setMaxInning(9);

        // ValidationUtil.validateDifferentTeams에서 예외가 발생하도록 모킹 (이 부분은 서비스 모킹이 아닌 컨트롤러의 직접적인 유효성 검사 로직을 따릅니다.)
        // 따라서 when().thenThrow()는 필요하지 않습니다. 컨트롤러의 try-catch가 직접 처리합니다.
        // when(gameService.createGame(anyString(), anyString())).thenThrow(new RuntimeException("홈팀과 원정팀은 동일할 수 없습니다."));

        mockMvc.perform(post("/api/baseball/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("홈팀과 원정팀은 서로 다른 팀이어야 합니다."))); // 메시지 내용 수정
    }

    /**
     * 게임 생성(createGame) API에서 유효하지 않은 이닝 수로 요청했을 때 예외를 테스트합니다.
     * GameService의 createGame 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void createGame_유효하지않은이닝수_예외() throws Exception {
        GameCreateRequest request = new GameCreateRequest();
        request.setHomeTeam("A팀");
        request.setAwayTeam("B팀");
        request.setMaxInning(0); // 유효하지 않은 이닝 수

        // ValidationUtil.validateMaxInning에서 예외가 발생하도록 모킹 (이 부분은 서비스 모킹이 아닌 컨트롤러의 직접적인 유효성 검사 로직을 따릅니다.)
        // 따라서 when().thenThrow()는 필요하지 않습니다. 컨트롤러의 try-catch가 직접 처리합니다.
        // when(gameService.createGame(anyString(), anyString())).thenThrow(new RuntimeException("이닝 수는 1 이상이어야 합니다."));

        mockMvc.perform(post("/api/baseball/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("최대 이닝 수는 3에서 9사이여야 합니다."))); // 메시지 내용 수정
    }


    /**
     * 게임 정보 조회(getGame) API의 성공 케이스를 테스트합니다.
     * GameService의 getGame 메서드를 모킹하여 예상 결과를 반환하도록 설정합니다.
     */
    @Test
    public void getGame_성공() throws Exception {
        GameDto gameDto = new GameDto();
        gameDto.setGameId("testId");

        when(gameService.getGame(anyString())).thenReturn(gameDto);

        mockMvc.perform(get("/api/baseball/game/testId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.game.gameId").value("testId"));
    }

    /**
     * 게임 정보 조회(getGame) API에서 없는 게임 ID를 요청했을 때 예외를 테스트합니다.
     * GameService의 getGame 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    void getGame_없는게임_예외() throws Exception {
        when(gameService.getGame(anyString())).thenThrow(new RuntimeException("게임을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/baseball/game/invalidId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임을 찾을 수 없습니다.")));
    }

    /**
     * 타격(batterSwing) API의 성공 케이스를 테스트합니다.
     * GameService의 batterSwing 및 getGame 메서드를 모킹하여 예상 결과를 반환하도록 설정합니다.
     */
    @Test
    public void batterSwing_성공() throws Exception {
        String gameId = "game123";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("swing", true); // 스윙 여부
        requestBody.put("timing", 0.7); // 타격 타이밍

        String swingResult = "안타"; // 타격 결과
        GameDto gameDtoAfterSwing = new GameDto();
        gameDtoAfterSwing.setGameId(gameId);
        // 필요한 경우 gameDtoAfterSwing의 다른 필드들도 설정 (예: 스코어, 아웃 카운트, 볼/스트라이크 등)

        // gameService.batterSwing 호출 시 예상 결과 반환하도록 모킹
        when(gameService.batterSwing(eq(gameId), anyBoolean(), anyDouble())).thenReturn(swingResult);
        // gameService.getGame 호출 시 업데이트된 게임 DTO 반환하도록 모킹
        when(gameService.getGame(eq(gameId))).thenReturn(gameDtoAfterSwing);

        // MockMvc를 사용하여 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/baseball/game/{gameId}/batter", gameId)
                .contentType(MediaType.APPLICATION_JSON) // 요청 본문 타입 설정
                .content(objectMapper.writeValueAsString(requestBody))) // 요청 본문 JSON으로 변환
                .andExpect(status().isOk()) // HTTP 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.success").value(true)) // 응답의 success 필드가 true인지 확인
                .andExpect(jsonPath("$.result").value(swingResult)) // 응답의 result 필드가 예상 결과와 일치하는지 확인
                .andExpect(jsonPath("$.game.gameId").value(gameId)); // 응답의 game.gameId 필드가 예상 gameId와 일치하는지 확인
    }

    /**
     * 타격(batterSwing) API에서 swing 값이 null일 때 예외를 테스트합니다.
     * GameService의 batterSwing 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void batterSwing_유효하지않은스윙값_예외() throws Exception {
        String gameId = "game123";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("swing", null); // 유효하지 않은 swing 값 (null)
        requestBody.put("timing", 0.5);

        // GameController의 batterSwing 메서드가 직접 "스윙 여부를 지정해주세요." 메시지를 반환하도록 변경되었으므로,
        // 서비스 모킹 대신 직접 컨트롤러의 응답을 검증합니다.
        // MockMvc는 컨트롤러의 로직을 직접 실행하므로 서비스 모킹이 필요 없습니다.
        mockMvc.perform(post("/api/baseball/game/{gameId}/batter", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("스윙 여부를 지정해주세요.")));
    }

    /**
     * 타격(batterSwing) API에서 timing 값이 유효한 범위를 벗어날 때 예외를 테스트합니다.
     * GameService의 batterSwing 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void batterSwing_유효하지않은타이밍_예외() throws Exception {
        String gameId = "game123";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("swing", true);
        requestBody.put("timing", 1.5); // 유효하지 않은 timing 값 (범위 초과)

        // GameService의 batterSwing이 예외를 던지도록 모킹
        when(gameService.batterSwing(eq(gameId), anyBoolean(), anyDouble())).thenThrow(new RuntimeException("타이밍 값은 0.0에서 1.0 사이여야 합니다."));

        mockMvc.perform(post("/api/baseball/game/{gameId}/batter", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("타격 처리 중 오류가 발생했습니다: 타이밍 값은 0.0에서 1.0 사이여야 합니다."))); // 메시지 내용 수정
    }

    /**
     * 타격(batterSwing) API에서 존재하지 않는 게임 ID로 요청했을 때 예외를 테스트합니다.
     * GameService의 getGame 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void batterSwing_없는게임_예외() throws Exception {
        String gameId = "invalidGameId";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("swing", true);
        requestBody.put("timing", 0.5);

        when(gameService.batterSwing(eq(gameId), anyBoolean(), anyDouble())).thenReturn("어떤 결과"); // 서비스는 정상 반환
        when(gameService.getGame(eq(gameId))).thenThrow(new RuntimeException("게임을 찾을 수 없습니다.")); // getGame에서 예외 발생

        mockMvc.perform(post("/api/baseball/game/{gameId}/batter", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임을 찾을 수 없습니다.")));
    }


    /**
     * 투구(pitcherThrow) API의 성공 케이스를 테스트합니다.
     * GameService의 pitcherThrow 및 getGame 메서드를 모킹하여 예상 결과를 반환하도록 설정합니다.
     */
    @Test
    public void pitcherThrow_성공() throws Exception {
        String gameId = "game123";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("pitchType", "직구"); // 투구 타입 (예: '직구', '변화구')

        String pitchResult = "스트라이크"; // 투구 결과
        GameDto gameDtoAfterPitch = new GameDto();
        gameDtoAfterPitch.setGameId(gameId);
        // 필요한 경우 gameDtoAfterPitch의 다른 필드들도 설정 (예: 볼/스트라이크 카운트, 아웃 등)

        // gameService.pitcherThrow 호출 시 예상 결과 반환하도록 모킹
        when(gameService.pitcherThrow(eq(gameId), anyString())).thenReturn(pitchResult);
        // gameService.getGame 호출 시 업데이트된 게임 DTO 반환하도록 모킹
        when(gameService.getGame(eq(gameId))).thenReturn(gameDtoAfterPitch);

        // MockMvc를 사용하여 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/baseball/game/{gameId}/pitcher", gameId)
                .contentType(MediaType.APPLICATION_JSON) // 요청 본문 타입 설정
                .content(objectMapper.writeValueAsString(requestBody))) // 요청 본문 JSON으로 변환
                .andExpect(status().isOk()) // HTTP 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.success").value(true)) // 응답의 success 필드가 true인지 확인
                .andExpect(jsonPath("$.result").value(pitchResult)) // 응답의 result 필드가 예상 결과와 일치하는지 확인
                .andExpect(jsonPath("$.game.gameId").value(gameId)); // 응답의 game.gameId 필드가 예상 gameId와 일치하는지 확인
    }

    /**
     * 투구(pitcherThrow) API에서 pitchType이 누락되었을 때 예외를 테스트합니다.
     */
    @Test
    public void pitcherThrow_투구타입누락_예외() throws Exception {
        String gameId = "game123";
        Map<String, String> requestBody = new HashMap<>();
        // requestBody.put("pitchType", "직구"); // pitchType 누락

        // GameController의 pitcherThrow 메서드가 직접 "투구 타입을 지정해주세요." 메시지를 반환하도록 변경되었으므로,
        // 서비스 모킹 대신 직접 컨트롤러의 응답을 검증합니다.
        mockMvc.perform(post("/api/baseball/game/{gameId}/pitcher", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("투구 타입을 지정해주세요.")));
    }

    /**
     * 투구(pitcherThrow) API에서 존재하지 않는 게임 ID로 요청했을 때 예외를 테스트합니다.
     * GameService의 pitcherThrow 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void pitcherThrow_없는게임_예외() throws Exception {
        String gameId = "invalidGameId";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("pitchType", "직구");

        when(gameService.pitcherThrow(eq(gameId), anyString())).thenReturn("어떤 결과"); // 서비스는 정상 반환
        when(gameService.getGame(eq(gameId))).thenThrow(new RuntimeException("게임을 찾을 수 없습니다.")); // getGame에서 예외 발생

        mockMvc.perform(post("/api/baseball/game/{gameId}/pitcher", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임을 찾을 수 없습니다.")));
    }


    /**
     * 다음 이닝(nextInning) API의 성공 케이스를 테스트합니다.
     * GameService의 nextInning 메서드를 모킹하여 다음 이닝으로 진행된 GameDto를 반환하도록 설정합니다.
     */
    @Test
    public void nextInning_성공() throws Exception {
        String gameId = "game123";
        GameDto gameDtoAfterNextInning = new GameDto();
        gameDtoAfterNextInning.setGameId(gameId);
        gameDtoAfterNextInning.setInning(2); // 다음 이닝으로 진행

        // gameService.nextInning 호출 시 예상 결과 반환하도록 모킹
        when(gameService.nextInning(eq(gameId))).thenReturn(gameDtoAfterNextInning);

        // MockMvc를 사용하여 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/baseball/game/{gameId}/next-inning", gameId))
                .andExpect(status().isOk()) // HTTP 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.success").value(true)) // 응답의 success 필드가 true인지 확인
                .andExpect(jsonPath("$.game.inning").value(2)) // 응답의 game.inning 필드가 2인지 확인
                .andExpect(jsonPath("$.message").value("다음 이닝으로 진행됩니다.")); // 응답 메시지 확인
    }

    /**
     * 다음 이닝(nextInning) API에서 존재하지 않는 게임 ID로 요청했을 때 예외를 테스트합니다.
     * GameService의 nextInning 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void nextInning_없는게임_예외() throws Exception {
        String gameId = "invalidGameId";

        when(gameService.nextInning(eq(gameId))).thenThrow(new RuntimeException("게임을 찾을 수 없습니다."));

        mockMvc.perform(post("/api/baseball/game/{gameId}/next-inning", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임을 찾을 수 없습니다.")));
    }


    /**
     * 게임 종료(endGame) API의 성공 케이스를 테스트합니다.
     * GameService의 endGame 메서드를 모킹하여 게임 종료 후의 GameDto를 반환하도록 설정합니다.
     */
    @Test
    public void endGame_성공() throws Exception {
        String gameId = "game123";
        GameDto gameDtoAfterEndGame = new GameDto();
        gameDtoAfterEndGame.setGameId(gameId);
        gameDtoAfterEndGame.setGameOver(true); // 게임 종료 상태
        gameDtoAfterEndGame.setWinner("HomeTeam"); // 승자 설정

        // gameService.endGame 호출 시 예상 결과 반환하도록 모킹
        when(gameService.endGame(eq(gameId))).thenReturn(gameDtoAfterEndGame);

        // MockMvc를 사용하여 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/baseball/game/{gameId}/end", gameId))
                .andExpect(status().isOk()) // HTTP 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.success").value(true)) // 응답의 success 필드가 true인지 확인
                .andExpect(jsonPath("$.game.gameOver").value(true)) // 응답의 game.gameOver 필드가 true인지 확인
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임이 종료되었습니다. 승자: HomeTeam"))); // 응답 메시지에 승자 정보가 포함되어 있는지 확인
    }

    /**
     * 게임 종료(endGame) API에서 존재하지 않는 게임 ID로 요청했을 때 예외를 테스트합니다.
     * GameService의 endGame 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void endGame_없는게임_예외() throws Exception {
        String gameId = "invalidGameId";

        when(gameService.endGame(eq(gameId))).thenThrow(new RuntimeException("게임을 찾을 수 없습니다."));

        mockMvc.perform(post("/api/baseball/game/{gameId}/end", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임을 찾을 수 없습니다.")));
    }


    /**
     * 베이스 러닝(advanceRunners) API의 성공 케이스를 테스트합니다.
     * GameService의 advanceRunners 및 getGame 메서드를 모킹하여 예상 결과를 반환하도록 설정합니다.
     */
    @Test
    public void advanceRunners_성공() throws Exception {
        String gameId = "game123";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("bases", 1); // 1루 진루

        GameDto gameDtoAfterAdvance = new GameDto();
        gameDtoAfterAdvance.setGameId(gameId);

        // Batter[] 타입에 맞게 com.baseball.game.dto.Batter 객체를 생성하여 배열에 할당합니다.
        // Batter 클래스에 public Batter() 기본 생성자가 있으므로 바로 new Batter()를 사용할 수 있습니다.
        Batter[] updatedBases = new Batter[4]; // 홈, 1루, 2루, 3루
        updatedBases[1] = new Batter(); // 1루에 새로운 Batter 객체 (주자) 배치
        gameDtoAfterAdvance.setBases(updatedBases);


        // advanceRunners가 void를 반환하므로 doNothing()을 사용합니다.
        doNothing().when(gameService).advanceRunners(eq(gameId), anyInt());
        when(gameService.getGame(eq(gameId))).thenReturn(gameDtoAfterAdvance);


        mockMvc.perform(post("/api/baseball/game/{gameId}/advance-runners", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.game.gameId").value(gameId))
                .andExpect(jsonPath("$.message").value("1베이스 진루했습니다."));
    }

    /**
     * 베이스 러닝(advanceRunners) API에서 진루할 베이스 수가 누락되었을 때 예외를 테스트합니다.
     */
    @Test
    public void advanceRunners_베이스수누락_예외() throws Exception {
        String gameId = "game123";
        Map<String, Integer> requestBody = new HashMap<>();
        // requestBody.put("bases", 1); // bases 값 누락

        // GameController의 advanceRunners 메서드가 직접 "진루할 베이스 수를 지정해주세요." 메시지를 반환하도록 변경되었으므로,
        // 서비스 모킹 대신 직접 컨트롤러의 응답을 검증합니다.
        mockMvc.perform(post("/api/baseball/game/{gameId}/advance-runners", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("진루할 베이스 수를 지정해주세요.")));
    }

    /**
     * 베이스 러닝(advanceRunners) API에서 존재하지 않는 게임 ID로 요청했을 때 예외를 테스트합니다.
     * GameService의 advanceRunners 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void advanceRunners_없는게임_예외() throws Exception {
        String gameId = "invalidGameId";
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("bases", 1);

        // advanceRunners가 void를 반환하므로 doNothing()을 사용하고, getGame에서 예외를 던지도록 설정
        doNothing().when(gameService).advanceRunners(eq(gameId), anyInt());
        when(gameService.getGame(eq(gameId))).thenThrow(new RuntimeException("게임을 찾을 수 없습니다."));


        mockMvc.perform(post("/api/baseball/game/{gameId}/advance-runners", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임을 찾을 수 없습니다.")));
    }


    /**
     * 게임 통계 조회(getGameStats) API의 성공 케이스를 테스트합니다.
     * GameService의 getGameStats 메서드를 모킹하여 예상 결과를 반환하도록 설정합니다.
     */
    @Test
    public void getGameStats_성공() throws Exception {
        String gameId = "game123";
        String statsResult = "홈팀: 10점, 원정팀: 5점, 안타: 15";

        when(gameService.getGameStats(eq(gameId))).thenReturn(statsResult);

        mockMvc.perform(get("/api/baseball/game/{gameId}/stats", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats").value(statsResult));
    }

    /**
     * 게임 통계 조회(getGameStats) API에서 존재하지 않는 게임 ID로 요청했을 때 예외를 테스트합니다.
     * GameService의 getGameStats 메서드가 RuntimeException을 발생시키도록 모킹합니다.
     */
    @Test
    public void getGameStats_없는게임_예외() throws Exception {
        String gameId = "invalidGameId";

        when(gameService.getGameStats(eq(gameId))).thenThrow(new RuntimeException("게임을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/baseball/game/{gameId}/stats", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("게임을 찾을 수 없습니다.")));
    }
}
