package com.baseball.game.util;

import com.baseball.game.exception.ValidationException;

public class ValidationUtil {

    public static void validateGameId(String gameId) {
        if (gameId == null || gameId.trim().isEmpty()) {
            throw new ValidationException("게임 ID는 필수입니다.");
        }

        if (!gameId.matches("^[a-zA-Z0-9-]+$")) {
            throw new ValidationException("게임 ID는 영문자, 숫자, 하이픈만 허용됩니다.");
        }
    }

    public static void validateTiming(Double timing) {
        if (timing == null) {
            throw new ValidationException("타이밍 값은 필수입니다.");
        }

        if (timing < 0.0 || timing > 1.0) {
            throw new ValidationException("타이밍 값은 0.0에서 1.0 사이여야 합니다.");
        }
    }

    public static void validateSwing(Boolean swing) {
        if (swing == null) {
            throw new ValidationException("스윙 여부는 필수입니다.");
        }
    }

    public static void validatePitchType(String pitchType) {
        if (pitchType == null || pitchType.trim().isEmpty()) {
            throw new ValidationException("투구 타입은 필수입니다.");
        }

        if (!pitchType.equals("strike") && !pitchType.equals("ball")) {
            throw new ValidationException("투구 타입은 'strike' 또는 'ball'이어야 합니다.");
        }
    }

    public static void validateBases(Integer bases) {
        if (bases == null) {
            throw new ValidationException("베이스 수는 필수입니다.");
        }

        if (bases < 1 || bases > 4) {
            throw new ValidationException("베이스 수는 1에서 4 사이여야 합니다.");
        }
    }

    public static void validateTeamName(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new ValidationException("팀명은 필수입니다.");
        }

        if (teamName.length() > 50) {
            throw new ValidationException("팀명은 50자를 초과할 수 없습니다.");
        }
    }

    public static void validateDifferentTeams(String homeTeam, String awayTeam) {
        if (homeTeam == null || awayTeam == null) {
            throw new ValidationException("홈팀과 원정팀 모두 필수입니다.");
        }

        if (homeTeam.trim().equalsIgnoreCase(awayTeam.trim())) {
            throw new ValidationException("홈팀과 원정팀은 서로 다른 팀이어야 합니다.");
        }
    }

    public static void validateMaxInning(Integer maxInning) {
        if (maxInning == null) {
            throw new ValidationException("최대 이닝 수는 필수입니다.");
        }

        if (maxInning < 3 || maxInning > 9) {
            throw new ValidationException("최대 이닝 수는 3에서 9사이여야 합니다.");
        }
    }
}