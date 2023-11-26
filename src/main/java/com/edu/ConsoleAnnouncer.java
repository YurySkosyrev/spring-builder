package com.edu;

public class ConsoleAnnouncer implements Announcer{
    @Override
    public void announce(String message) {
        System.out.println(message);
    }
}
