package com.baseball.game.exception;

public class InvalidGameStateException extends GameException {

    public InvalidGameStateException(String message) {
        super(message);
    }
}