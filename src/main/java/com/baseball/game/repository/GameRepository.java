package com.baseball.game.repository;

import com.baseball.game.dto.GameDto;
import java.util.List;
import java.util.Optional;

public interface GameRepository {

    /**
     * 게임 저장
     */
    void save(GameDto game);

    /**
     * 게임 ID로 조회
     */
    Optional<GameDto> findById(String gameId);

    /**
     * 모든 게임 조회
     */
    List<GameDto> findAll();

    /**
     * 게임 삭제
     */
    void delete(String gameId);

    /**
     * 게임 존재 여부 확인
     */
    boolean exists(String gameId);

    /**
     * 활성 게임 수 조회
     */
    long countActiveGames();

    /**
     * 만료된 게임 정리 (24시간 이상 된 게임)
     */
    void cleanupExpiredGames();
}