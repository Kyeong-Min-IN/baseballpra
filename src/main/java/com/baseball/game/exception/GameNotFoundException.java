package com.baseball.game.exception;

public class GameNotFoundException extends GameException {

    public GameNotFoundException(String gameId) {
        super("게임을 찾을 수 없습니다. GameId: " + gameId);
    }
}