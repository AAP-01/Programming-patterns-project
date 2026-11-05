package vanier.conference_management_system.model;

public class Credential {

    private int userId;
    private String username;
    private String passwordHash;

    public Credential() {
    }

    public Credential(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() {
        return "Credential{userId=" + userId + ", username=" + username + "}";
    }
}