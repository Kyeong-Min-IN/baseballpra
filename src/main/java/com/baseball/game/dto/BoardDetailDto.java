package com.baseball.game.dto;

import lombok.Data;
import java.util.ArrayList;

@Data
public class BoardDetailDto {
    private BoardDto board;
    private ArrayList<CommentDto> comments;

    public BoardDetailDto(BoardDto board, ArrayList<CommentDto> comments) {
        this.board = board;
        this.comments = comments;
    }
}