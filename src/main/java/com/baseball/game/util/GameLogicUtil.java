package com.baseball.game.util;

import com.baseball.game.dto.Batter;
import com.baseball.game.dto.Pitcher;
import com.baseball.game.dto.GameDto;

public class GameLogicUtil {
    /**
     * 투수의 제구력에 따라 실제 결과가 바뀌는 투구 결과 결정
     */
    public static String determinePitchResult(Pitcher pitcher, String pitchType) {
        int control = pitcher.getControl();
        double baseProb = 0.7 + (control - 50) * 0.006; // 제구력 50: 70%, 100: 100%, 0: 40%
        baseProb = Math.max(0.4, Math.min(1.0, baseProb));
        double rand = Math.random();
        if (rand < baseProb) {
            return pitchType.equals("strike") ? "스트라이크" : "볼";
        } else {
            return pitchType.equals("strike") ? "볼" : "스트라이크";
        }
    }

    /**
     * 타자와 투수 능력치, 타이밍을 모두 고려하여 타격 결과를 결정하는 개선된 로직
     * @param timing: 0.0 ~ 1.0 (0.5가 정타)
     */
    public static String determineHitResult(Batter batter, Pitcher pitcher, double timing) {
        // 1. 타이밍 가중치 계산 (정타일수록 높은 값)
        double timingWeight = 1.0 - Math.abs(timing - 0.5) * 2; // 0.5일 때 1.0, 0 또는 1일 때 0.0

        // 2. 타자 능력치 점수 계산 (컨택과 파워 반영)
        double batterScore = (batter.getContact() * 0.6) + (batter.getPower() * 0.4);

        // 3. 투수 능력치 점수 계산 (제구와 구속 반영)
        double pitcherScore = (pitcher.getControl() * 0.5) + (pitcher.getSpeed() * 0.5);

        // 4. 최종 타격 점수 계산
        // (타자 점수 - 투수 점수)를 기반으로, 타이밍에 따라 보정
        double finalScore = (batterScore - pitcherScore) * 0.5 + 50; // 기본 점수 50점
        finalScore *= (0.7 + timingWeight * 0.6); // 타이밍 가중치를 0.7 ~ 1.3 사이로 반영

        // 5. 최종 점수에 따른 결과 결정
        if (finalScore > 95) return "홈런!";
        if (finalScore > 90) return "3루타";
        if (finalScore > 80) return "2루타";
        if (finalScore > 60) return "안타";
        if (finalScore > 40) return "땅볼 아웃";
        if (finalScore > 20) return "뜬공 아웃";
        return "헛스윙 삼진"; // 점수가 매우 낮으면 헛스윙
    }
    /**
     * 타이밍을 반영한 타격 결과 결정
     * timing: 0.0 ~ 1.0 (0.5가 정타)
     */
    public static String determineHitResultWithTiming(boolean swing, Pitcher pitcher, String pitchType, double timing,
            Batter batter) {
        if (!swing) {
            return determinePitchResult(pitcher, pitchType);
        }

        // 투수가 볼을 던졌을 때 컨택 능력치에 따른 처리
        if ("ball".equals(pitchType)) {
            // 컨택 능력치에 따라 볼을 맞출 확률 계산
            double contactChance = 0.1 + (batter.getContact() - 50) * 0.01; // 컨택 50: 10%, 100: 60%
            contactChance = Math.max(0.05, Math.min(0.8, contactChance)); // 5%~80% 범위

            if (Math.random() < contactChance) {
                // 볼을 맞췄으면 타격 결과로 처리
                return determineHitResultByTiming(timing);
            } else {
                // 볼을 못 맞췄으면 헛스윙
                return "헛스윙";
            }
        }

        // 스트라이크 존 공에 대한 헛스윙 확률 계산 (타이밍이 나쁠수록 헛스윙 확률 증가)
        double missChance = 0.0;
        if (timing < 0.3 || timing > 0.7) {
            missChance = 0.4; // 매우 나쁜 타이밍: 40% 헛스윙
        } else if (timing < 0.4 || timing > 0.6) {
            missChance = 0.2; // 나쁜 타이밍: 20% 헛스윙
        } else if (timing < 0.45 || timing > 0.55) {
            missChance = 0.1; // 준정타: 10% 헛스윙
        } else {
            missChance = 0.05; // 정타: 5% 헛스윙
        }

        // 헛스윙 체크
        if (Math.random() < missChance) {
            return "헛스윙";
        }

        return determineHitResultByTiming(timing);
    }

    /**
     * 타이밍에 따른 타격 결과 결정 (볼 스윙 처리 후 사용)
     */
    private static String determineHitResultByTiming(double timing) {
        double rand = Math.random();
        if (timing >= 0.45 && timing <= 0.55) {
            // 정타: 장타 확률 증가
            if (rand < 0.10) {
                return "땅볼 아웃";
            } else if (rand < 0.30) {
                return "뜬공 아웃";
            } else if (rand < 0.55) {
                return "안타";
            } else if (rand < 0.75) {
                return "2루타";
            } else if (rand < 0.90) {
                return "3루타";
            } else {
                return "홈런";
            }
        } else if (timing >= 0.35 && timing <= 0.65) {
            // 준정타: 기본 확률
            if (rand < 0.15) {
                return "땅볼 아웃";
            } else if (rand < 0.55) {
                return "뜬공 아웃";
            } else if (rand < 0.75) {
                return "안타";
            } else if (rand < 0.85) {
                return "2루타";
            } else if (rand < 0.95) {
                return "3루타";
            } else {
                return "홈런";
            }
        } else {
            // 헛스윙: 아웃 확률 증가
            if (rand < 0.20) {
                return "땅볼 아웃";
            } else if (rand < 0.70) {
                return "뜬공 아웃";
            } else if (rand < 0.80) {
                return "안타";
            } else if (rand < 0.90) {
                return "2루타";
            } else if (rand < 0.97) {
                return "3루타";
            } else {
                return "홈런";
            }
        }
    }

    /**
     * 땅볼 처리: 병살/진루/아웃
     */
    public static String processGroundBall(GameDto game, Batter batter) {
        Batter[] bases = game.getBases();
        int out = game.getOut();
        // 2아웃이면 병살 불가, 타자만 아웃
        if (out == 2) {
            game.setOut(out + 1);
            return "땅볼 아웃, 주자 진루 없음";
        }
        // 1루에 주자 있는 경우
        if (bases[1] != null) {
            if (Math.random() < 0.3) { // 30% 병살
                game.setOut(out + 2);
                bases[1] = null;
                return "병살타!";
            } else {
                game.setOut(out + 1);
                bases[2] = bases[1];
                bases[1] = null;
                return "땅볼 아웃, 1루 주자 2루 진루";
            }
        } else {
            game.setOut(out + 1);
            return "땅볼 아웃, 주자 진루 없음";
        }
    }

    public static void resetBases(GameDto game) {
        game.setBases(new Batter[4]);
        game.getBaseRunners().clear();
    }

    public static void addRunnerToBase(GameDto game, int base, Batter runner) {
        if (base >= 0 && base <= 3) {
            Batter[] bases = game.getBases();
            bases[base] = runner;
            if (base > 0) {
                game.getBaseRunners().add(runner);
            }
        }
    }

    public static void advanceRunners(GameDto game, int bases) {
        Batter[] currentBases = game.getBases();
        for (int i = 3; i >= 0; i--) {
            if (currentBases[i] != null) {
                int newBase = Math.min(i + bases, 3);
                if (newBase == 3) {
                    game.setHomeScore(game.getHomeScore() + 1);
                }
                currentBases[newBase] = currentBases[i];
                if (i != newBase) {
                    currentBases[i] = null;
                }
            }
        }
    }
}