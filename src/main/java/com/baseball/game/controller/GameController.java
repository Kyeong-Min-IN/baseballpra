// src/main/java/com/baseball/game/controller/GameController.java
package com.baseball.game.controller;

import com.baseball.game.dto.GameCreateRequest;
import com.baseball.game.dto.GameDto;
import com.baseball.game.dto.TeamLineupSetRequest; // 새로 추가될 DTO
import com.baseball.game.service.GameService;
import com.baseball.game.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Setter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/baseball")
@CrossOrigin(origins = "*")
public class GameController {

	private static final Logger logger = LoggerFactory.getLogger(GameController.class);

	@Setter(onMethod_ = @Autowired)
	private GameService service;

	/**
	 * 게임 생성 (이닝 수, 사용자 공격 여부 포함)
	 * 요청 본문으로 홈팀, 원정팀, 최대 이닝, 사용자 공격 여부를 받아 게임을 생성합니다.
	 * 유효성 검사 실패 시 또는 서비스 로직에서 예외 발생 시 실패 응답을 반환합니다.
	 * @param request 게임 생성 요청 DTO (홈팀, 원정팀, 최대 이닝, 사용자 공격 여부 포함)
	 * @return 성공 시 생성된 GameDto를 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@PostMapping("/game")
	public Map<String, Object> createGame(@RequestBody GameCreateRequest request) { // 반환 타입을 Map으로 변경
	    Map<String, Object> response = new HashMap<>();
	    try {
	        // ValidationUtil을 사용하여 유효성 검사 수행
	        ValidationUtil.validateTeamName(request.getHomeTeam());
	        ValidationUtil.validateTeamName(request.getAwayTeam());
	        ValidationUtil.validateDifferentTeams(request.getHomeTeam(), request.getAwayTeam());
	        ValidationUtil.validateMaxInning(request.getMaxInning());

	        GameDto newGame = service.createGame(request.getHomeTeam(), request.getAwayTeam(), request.getMaxInning(), request.isIsUserOffense());
	        response.put("success", true);
	        response.put("game", newGame);
	        response.put("message", String.format("게임이 생성되었습니다. (ID: %s)", newGame.getGameId()));
	        logger.info("새로운 게임 생성: {}", newGame.getGameId());
	    } catch (Exception e) {
	        logger.error("게임 생성 중 오류 발생: {}", e.getMessage());
	        response.put("success", false);
	        response.put("message", "게임 생성 중 오류가 발생했습니다: " + e.getMessage());
	    }
	    return response;
	}

	/**
	 * 특정 팀의 라인업과 선발 투수를 설정합니다.
	 * @param gameId 게임 ID
	 * @param request 라인업 및 선발 투수 정보를 담은 요청 DTO
	 * @return 성공 시 업데이트된 GameDto를 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@PostMapping("/game/{gameId}/lineup")
	public Map<String, Object> setTeamLineup(@PathVariable String gameId, @RequestBody TeamLineupSetRequest request) {
		Map<String, Object> response = new HashMap<>();
		try {
			if (request.getTeamName() == null || request.getTeamName().trim().isEmpty()) {
				throw new IllegalArgumentException("팀 이름은 필수입니다.");
			}
			if (request.getBattingOrder() == null || request.getBattingOrder().isEmpty()) {
				throw new IllegalArgumentException("타순은 필수입니다.");
			}
			if (request.getStartingPitcher() == null || request.getStartingPitcher().trim().isEmpty()) {
				throw new IllegalArgumentException("선발 투수는 필수입니다.");
			}

			service.setTeamLineupAndPitcher(gameId, request.getTeamName(), request.getBattingOrder(), request.getStartingPitcher());
			GameDto game = service.getGame(gameId); // 업데이트된 게임 상태 가져오기

			response.put("success", true);
			response.put("game", game);
			response.put("message", request.getTeamName() + " 팀의 라인업과 선발 투수가 성공적으로 설정되었습니다.");
			logger.info("게임 {}: {} 팀 라인업 및 선발 투수 설정 완료.", gameId, request.getTeamName());

		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "팀 라인업 및 선발 투수 설정 중 오류가 발생했습니다: " + e.getMessage());
			logger.error("팀 라인업 및 선발 투수 설정 중 오류 발생: {}", e.getMessage());
		}
		return response;
	}


	/**
	 * 타자 스윙/노스윙 처리
	 * @param gameId 게임 ID
	 * @param swing 스윙 여부 (true: 스윙, false: 노스윙)
	 * @param timing 타이밍 (0.0 ~ 1.0, 스윙 시 유효)
	 * @return 성공 시 결과 메시지를 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@PostMapping("/game/{gameId}/swing")
	public Map<String, Object> batterSwing(@PathVariable String gameId, @RequestBody Map<String, Object> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			Boolean swing = (Boolean) request.get("swing");
			Double timing = null;
			if (request.containsKey("timing")) {
				Object timingObj = request.get("timing");
				if (timingObj instanceof Integer) { // JSON에서 정수로 넘어올 경우 대비
					timing = ((Integer) timingObj).doubleValue();
				} else if (timingObj instanceof Double) {
					timing = (Double) timingObj;
				}
			}

			if (swing == null) {
				response.put("success", false);
				response.put("message", "스윙 여부를 지정해주세요 (swing: true/false).");
				return response;
			}
			if (swing && timing == null) {
				response.put("success", false);
				response.put("message", "스윙 시 타이밍을 지정해주세요 (timing: 0.0 ~ 1.0).");
				return response;
			}
			if (timing != null && (timing < 0.0 || timing > 1.0)) {
				response.put("success", false);
				response.put("message", "타이밍은 0.0에서 1.0 사이여야 합니다.");
				return response;
			}

			String result = service.batterSwing(gameId, swing, timing);
			GameDto game = service.getGame(gameId); // 업데이트된 게임 상태 가져오기

			response.put("success", true);
			response.put("result", result);
			response.put("game", game); // 업데이트된 게임 DTO 반환
			response.put("message", "스윙/노스윙 처리 완료: " + result);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "스윙 처리 중 오류가 발생했습니다: " + e.getMessage());
		}
		return response;
	}

	/**
	 * 투수 투구 처리
	 * @param gameId 게임 ID
	 * @param request 투구 유형을 담은 Map
	 * @return 성공 시 결과 메시지를 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@PostMapping("/game/{gameId}/pitch")
	public Map<String, Object> pitcherThrow(@PathVariable String gameId, @RequestBody Map<String, String> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String pitchType = request.get("pitchType");
			if (pitchType == null || pitchType.trim().isEmpty()) {
				response.put("success", false);
				response.put("message", "투구 유형을 지정해주세요 (pitchType: 'strike' 또는 'ball').");
				return response;
			}
			if (!pitchType.equals("strike") && !pitchType.equals("ball")) {
				response.put("success", false);
				response.put("message", "유효하지 않은 투구 유형입니다. 'strike' 또는 'ball'을 사용해주세요.");
				return response;
			}

			String result = service.pitcherThrow(gameId, pitchType);
			GameDto game = service.getGame(gameId); // 업데이트된 게임 상태 가져오기

			response.put("success", true);
			response.put("result", result);
			response.put("game", game); // 업데이트된 게임 DTO 반환
			response.put("message", "투구 처리 완료: " + result);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "투구 처리 중 오류가 발생했습니다: " + e.getMessage());
		}
		return response;
	}

	/**
	 * 다음 이닝으로 진행
	 * @param gameId 게임 ID
	 * @return 성공 시 업데이트된 GameDto를 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@PostMapping("/game/{gameId}/next-inning")
	public Map<String, Object> nextInning(@PathVariable String gameId) {
		Map<String, Object> response = new HashMap<>();
		try {
			GameDto game = service.nextInning(gameId);
			response.put("success", true);
			response.put("game", game);
			if (game.isGameOver()) {
				response.put("message", "다음 이닝으로 진행되었습니다. 게임 종료! 승자: " + game.getWinner());
			} else {
				response.put("message", String.format("%d회 %s로 진행되었습니다.", game.getInning(), game.isTop() ? "초" : "말"));
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "다음 이닝 진행 중 오류가 발생했습니다: " + e.getMessage());
		}
		return response;
	}

	/**
	 * 게임 상태 조회
	 * @param gameId 게임 ID
	 * @return 성공 시 GameDto를 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@GetMapping("/game/{gameId}")
	public Map<String, Object> getGame(@PathVariable String gameId) {
		Map<String, Object> response = new HashMap<>();
		try {
			GameDto game = service.getGame(gameId);
			response.put("success", true);
			response.put("game", game);
			response.put("message", "게임 정보를 성공적으로 조회했습니다.");
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "게임 정보를 찾을 수 없습니다: " + e.getMessage());
		}
		return response;
	}

	/**
	 * 주자 진루 처리
	 * @param gameId 게임 ID
	 * @param request 진루할 베이스 수를 담은 Map
	 * @return 성공 시 업데이트된 GameDto를 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@PostMapping("/game/{gameId}/advance-runners")
	public Map<String, Object> advanceRunners(@PathVariable String gameId, @RequestBody Map<String, Integer> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			Integer bases = request.get("bases");
			if (bases == null) {
				response.put("success", false);
				response.put("message", "진루할 베이스 수를 지정해주세요.");
				return response;
			}

			service.advanceRunners(gameId, bases);
			GameDto game = service.getGame(gameId);

			response.put("success", true);
			response.put("game", game);
			response.put("message", bases + "베이스 진루했습니다.");
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "베이스 러닝 처리 중 오류가 발생했습니다: " + e.getMessage());
		}
		return response;
	}

	/**
	 * 게임 통계 조회
	 * @param gameId 게임 ID
	 * @return 성공 시 게임 통계 문자열을 포함하는 Map, 실패 시 success: false와 메시지를 포함하는 Map
	 */
	@GetMapping("/game/{gameId}/stats")
	public Map<String, Object> getGameStats(@PathVariable String gameId) {
		Map<String, Object> response = new HashMap<>();
		try {
			String stats = service.getGameStats(gameId);
			response.put("success", true);
			response.put("stats", stats);
			response.put("message", "게임 통계를 성공적으로 조회했습니다.");
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "게임 통계 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
		return response;
	}
}