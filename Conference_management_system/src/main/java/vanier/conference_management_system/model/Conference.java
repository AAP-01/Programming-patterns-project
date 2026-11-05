package vanier.conference_management_system.model;

import java.time.LocalDate;

public class Conference {

    private int conferenceId;
    private String title;
    private String description;
    private String venue;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationOpenDate;
    private LocalDate registrationCloseDate;
    private int capacity;
    private ConferenceStatus status;
    private String createdAt;

    public Conference() {
    }

    public Conference(String title, String description, String venue,
                      LocalDate startDate, LocalDate endDate,
                      LocalDate registrationOpenDate, LocalDate registrationCloseDate,
                      int capacity, ConferenceStatus status) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationOpenDate = registrationOpenDate;
        this.registrationCloseDate = registrationCloseDate;
        this.capacity = capacity;
        this.status = status;
    }

    public int getConferenceId() { return conferenceId; }
    public void setConferenceId(int conferenceId) { this.conferenceId = conferenceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getRegistrationOpenDate() { return registrationOpenDate; }
    public void setRegistrationOpenDate(LocalDate registrationOpenDate) { this.registrationOpenDate = registrationOpenDate; }

    public LocalDate getRegistrationCloseDate() { return registrationCloseDate; }
    public void setRegistrationCloseDate(LocalDate registrationCloseDate) { this.registrationCloseDate = registrationCloseDate; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public ConferenceStatus getStatus() { return status; }
    public void setStatus(ConferenceStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Conference{id=" + conferenceId + ", title=" + title
                + ", capacity=" + capacity + ", status=" + status + "}";
    }
}