package com.baseball.game.repository;

import com.baseball.game.dto.GameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisGameRepository implements GameRepository {

    private static final Logger logger = LoggerFactory.getLogger(RedisGameRepository.class);
    private static final String GAME_KEY_PREFIX = "game:";
    private static final String GAME_INDEX_KEY = "games:active";
    private static final long GAME_EXPIRY_HOURS = 24; // 24시간 후 만료

    @Setter(onMethod_ = @Autowired)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(GameDto game) {
        try {
            String gameKey = GAME_KEY_PREFIX + game.getGameId();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // 게임 데이터 저장
            redisTemplate.opsForValue().set(gameKey, game, GAME_EXPIRY_HOURS, TimeUnit.HOURS);

            // 활성 게임 인덱스에 추가
            redisTemplate.opsForSet().add(GAME_INDEX_KEY, game.getGameId());

            logger.info("게임 저장 완료: gameId={}, timestamp={}", game.getGameId(), timestamp);
        } catch (Exception e) {
            logger.error("게임 저장 실패: gameId={}", game.getGameId(), e);
            throw new RuntimeException("게임 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Optional<GameDto> findById(String gameId) {
        try {
            String gameKey = GAME_KEY_PREFIX + gameId;
            GameDto game = (GameDto) redisTemplate.opsForValue().get(gameKey);

            if (game != null) {
                logger.debug("게임 조회 성공: gameId={}", gameId);
                return Optional.of(game);
            } else {
                logger.debug("게임을 찾을 수 없음: gameId={}", gameId);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("게임 조회 실패: gameId={}", gameId, e);
            throw new RuntimeException("게임 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<GameDto> findAll() {
        try {
            Set<Object> gameIds = redisTemplate.opsForSet().members(GAME_INDEX_KEY);
            if (gameIds == null || gameIds.isEmpty()) {
                return List.of();
            }

            return gameIds.stream()
                    .map(id -> findById(id.toString()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("모든 게임 조회 실패", e);
            throw new RuntimeException("모든 게임 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void delete(String gameId) {
        try {
            String gameKey = GAME_KEY_PREFIX + gameId;

            // 게임 데이터 삭제
            redisTemplate.delete(gameKey);

            // 활성 게임 인덱스에서 제거
            redisTemplate.opsForSet().remove(GAME_INDEX_KEY, gameId);

            logger.info("게임 삭제 완료: gameId={}", gameId);
        } catch (Exception e) {
            logger.error("게임 삭제 실패: gameId={}", gameId, e);
            throw new RuntimeException("게임 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public boolean exists(String gameId) {
        try {
            String gameKey = GAME_KEY_PREFIX + gameId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(gameKey));
        } catch (Exception e) {
            logger.error("게임 존재 확인 실패: gameId={}", gameId, e);
            return false;
        }
    }

    @Override
    public long countActiveGames() {
        try {
            Set<Object> gameIds = redisTemplate.opsForSet().members(GAME_INDEX_KEY);
            return gameIds != null ? gameIds.size() : 0;
        } catch (Exception e) {
            logger.error("활성 게임 수 조회 실패", e);
            return 0;
        }
    }

    @Override
    public void cleanupExpiredGames() {
        try {
            Set<Object> gameIds = redisTemplate.opsForSet().members(GAME_INDEX_KEY);
            if (gameIds == null || gameIds.isEmpty()) {
                return;
            }

            int cleanedCount = 0;
            for (Object gameId : gameIds) {
                String gameKey = GAME_KEY_PREFIX + gameId.toString();
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(gameKey))) {
                    // 만료된 게임을 인덱스에서 제거
                    redisTemplate.opsForSet().remove(GAME_INDEX_KEY, gameId);
                    cleanedCount++;
                }
            }

            if (cleanedCount > 0) {
                logger.info("만료된 게임 정리 완료: {}개", cleanedCount);
            }
        } catch (Exception e) {
            logger.error("만료된 게임 정리 실패", e);
        }
    }
}