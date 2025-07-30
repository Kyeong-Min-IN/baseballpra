package com.baseball.game.dto;

import lombok.Data;

@Data
public class Batter extends Player {
	private int power; // 파워 (홈런 능력)
	private int contact; // 컨택트 (안타 능력)
	private int speed; // 스피드 (도루, 베이스 러닝)
	private int eye; // 선구안 (볼/스트라이크 판단)
	private int battingOrder; // 타순
	private int atBats; // 타수
	private int hits; // 안타
	private int homeRuns; // 홈런
	private int rbis; // 타점
	private double battingAverage; // 타율

	public Batter() {
		this.atBats = 0;
		this.hits = 0;
		this.homeRuns = 0;
		this.rbis = 0;
		this.battingAverage = 0.0;
	}
}
