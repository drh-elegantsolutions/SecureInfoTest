package uk.gov.dwp.carersallowance.sensitiveinfo;

public class SensitiveInformation {
    private String message;

    public SensitiveInformation(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}