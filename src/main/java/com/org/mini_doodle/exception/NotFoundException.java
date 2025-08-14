package com.org.mini_doodle.exception;

public class NotFoundException extends RuntimeException{

    public NotFoundException(String slotNotFound) {
        super(slotNotFound);
    }
}
