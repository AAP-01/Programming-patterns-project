package vanier.conference_management_system.model;

public class Registration {

    private int registrationId;
    private int conferenceId;
    private int userId;
    private RegistrationStatus status;
    private Integer waitingPosition;
    private String registeredAt;

    public Registration() {
    }

    public Registration(int conferenceId, int userId, RegistrationStatus status, Integer waitingPosition) {
        this.conferenceId = conferenceId;
        this.userId = userId;
        this.status = status;
        this.waitingPosition = waitingPosition;
    }

    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }

    public int getConferenceId() { return conferenceId; }
    public void setConferenceId(int conferenceId) { this.conferenceId = conferenceId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    public Integer getWaitingPosition() { return waitingPosition; }
    public void setWaitingPosition(Integer waitingPosition) { this.waitingPosition = waitingPosition; }

    public String getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }

    @Override
    public String toString() {
        return "Registration{id=" + registrationId + ", conference=" + conferenceId
                + ", user=" + userId + ", status=" + status
                + ", waitingPosition=" + waitingPosition + "}";
    }
}