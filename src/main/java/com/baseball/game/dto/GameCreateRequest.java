package com.baseball.game.dto;

import lombok.Data;

@Data
public class GameCreateRequest {
    private String homeTeam;
    private String awayTeam;
    private boolean IsUserOffense;
    private int maxInning; // 사용자가 선택한 이닝 수
    private boolean enableTiming; // 타이밍 시스템 활성화 여부
}