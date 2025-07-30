package com.baseball.game.dto;

import lombok.Data;

@Data
public class Pitcher extends Player {
	private int control; // 컨트롤 (볼/스트라이크 정확도)
	private int speed; // 구속 (공의 속도)
	private int stamina; // 체력 (이닝 소화 능력)
	private int movement; // 무브먼트 (공의 변화)
	private int inningsPitched; // 이닝 수
	private int strikeouts; // 삼진
	private int walks; // 볼넷
	private int hits; // 피안타
	private int earnedRuns; // 자책점
	private double era; // 평균자책점

	public Pitcher() {
		this.inningsPitched = 0;
		this.strikeouts = 0;
		this.walks = 0;
		this.hits = 0;
		this.earnedRuns = 0;
		this.era = 0.0;
	}
}
