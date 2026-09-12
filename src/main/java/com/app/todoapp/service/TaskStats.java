package com.app.todoapp.service;

public record TaskStats(long total , long completed) {

    public int percentComplete() {
        if (total == 0) return 0;
        return (int) Math.round((completed * 100.0) / total);
    }
}
