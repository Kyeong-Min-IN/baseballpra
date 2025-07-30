package com.baseball.game.dto;

import lombok.Data;
import java.util.ArrayList;

@Data
public class BoardPageResponse {
    private int totalCount;
    private ArrayList<BoardDto> list;

    public BoardPageResponse(int totalCount, ArrayList<BoardDto> list) {
        this.totalCount = totalCount;
        this.list = list;
    }
}