package io.github.palexdev.materialfx.demo.services;

public enum MatchStatusEnum {

    LIVE("Live"),
    UPCOMING("Upcoming"),
    FINISHED("Finished");

    private String displayValue;

    MatchStatusEnum(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static MatchStatusEnum fromDisplayValue(String displayValue) {
        for (MatchStatusEnum status : values()) {
            if (status.getDisplayValue().equals(displayValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown display value: " + displayValue);
    }

    @Override
    public String toString() {
        return displayValue;
    }
}