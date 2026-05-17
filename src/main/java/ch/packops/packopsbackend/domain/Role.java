package ch.packops.packopsbackend.domain;

public final class Role {

    public static final String ADMIN = "admin";
    public static final String OPERATOR = "operator";
    public static final String VIEWER = "viewer";

    private Role() {
    }

    public static String matchRole(String input) {
        return switch (input.toUpperCase()) {
            case "ADMIN" -> Role.ADMIN;
            case "OPERATOR" -> Role.OPERATOR;
            case "VIEWER" -> Role.VIEWER;
            default -> "Error";
        };
    }
}