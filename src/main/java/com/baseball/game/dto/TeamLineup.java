package com.baseball.game.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamLineup {
    private Integer id;
    private String teamName;
    private String lineupType; // "DEFAULT" 또는 "USER_CUSTOM"
    private String userId;
    private Integer position; // 타순 (1~9)
    private String playerName;
    private Integer playerId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}