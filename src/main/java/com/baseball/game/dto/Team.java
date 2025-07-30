package com.baseball.game.dto;

import lombok.Data;
import java.util.List;

@Data
public class Team {
	private List<Batter> Batter;
	private List<Pitcher> Pitcher;
}
