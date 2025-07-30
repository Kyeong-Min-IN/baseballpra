package com.baseball.game.dto;
import lombok.Data;
@Data
public class MemberDto {
	private String Id;
	private String Pw;
	private String Team;
	private int Game;
	private int Win;
	private int Lose;
}
