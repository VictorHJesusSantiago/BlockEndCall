package com.blockendcall.enums;

public enum BadgeType {
    FIRST_REPORT("Primeiro Reporte"),
    REPORTER_10("Repórter Ativo"),
    REPORTER_50("Repórter Dedicado"),
    REPORTER_100("Guardião da Comunidade"),
    REPORTER_500("Lenda"),
    FIRST_CONFIRMED("Confirmador"),
    STREAK_7("Semana Ativa"),
    EARLY_ADOPTER("Pioneiro");

    private final String displayName;

    BadgeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
