package com.baseball.game.dto;
import lombok.Data;
@Data
public class CommentDto {
	private int commentId;
	private int boardNo;
	private String writer;
	private String text;
	private String date;
}
