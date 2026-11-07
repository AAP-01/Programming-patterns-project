package vanier.conference_management_system.dao;

import vanier.conference_management_system.database.DataAccessException;
import vanier.conference_management_system.database.Database;
import vanier.conference_management_system.model.Conference;
import vanier.conference_management_system.model.ConferenceStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConferenceDao {

    private static final Logger LOGGER = Logger.getLogger(ConferenceDao.class.getName());
    private static final String INSERT = "INSERT INTO conferences (title, description, venue, start_date, end_date, " + "registration_open_date, registration_close_date, capacity, status) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM conferences WHERE conference_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM conferences ORDER BY start_date";
    private static final String SELECT_BY_STATUS = "SELECT * FROM conferences WHERE status = ? ORDER BY start_date";
    private static final String UPDATE = "UPDATE conferences SET title = ?, description = ?, venue = ?, start_date = ?, end_date = ?, " + " registration_open_date = ?, registration_close_date = ?, capacity = ?, status = ? " + "WHERE conference_id = ?";
    private static final String UPDATE_STATUS = "UPDATE conferences SET status = ? WHERE conference_id = ?";
    private static final String DELETE = "DELETE FROM conferences WHERE conference_id = ?";

    public Conference create(Conference conference) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setFields(ps, conference);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    conference.setConferenceId(keys.getInt(1));
                }
            }
            return conference;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not create conference " + conference.getTitle(), e);
            throw new DataAccessException("Could not create conference", e);
        }
    }

    public Optional<Conference> findById(int conferenceId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not find conference " + conferenceId, e);
            throw new DataAccessException("Could not find conference", e);
        }
    }

    public List<Conference> findAll() {
        List<Conference> conferences = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conferences.add(mapRow(rs));
            }
            return conferences;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not list conferences", e);
            throw new DataAccessException("Could not list conferences", e);
        }
    }

    public List<Conference> findByStatus(ConferenceStatus status) {
        List<Conference> conferences = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    conferences.add(mapRow(rs));
                }
            }
            return conferences;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not list conferences with status " + status, e);
            throw new DataAccessException("Could not list conferences", e);
        }
    }

    public boolean update(Conference conference) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            setFields(ps, conference);
            ps.setInt(10, conference.getConferenceId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not update conference " + conference.getConferenceId(), e);
            throw new DataAccessException("Could not update conference", e);
        }
    }

    public boolean updateStatus(int conferenceId, ConferenceStatus status) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setInt(2, conferenceId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not update status of conference " + conferenceId, e);
            throw new DataAccessException("Could not update conference status", e);
        }
    }

    public boolean delete(int conferenceId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE)) {
            ps.setInt(1, conferenceId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not delete conference " + conferenceId, e);
            throw new DataAccessException("Could not delete conference", e);
        }
    }

    private void setFields(PreparedStatement ps, Conference c) throws SQLException {
        ps.setString(1, c.getTitle());
        ps.setString(2, c.getDescription());
        ps.setString(3, c.getVenue());
        ps.setString(4, c.getStartDate().toString());
        ps.setString(5, c.getEndDate().toString());
        ps.setString(6, c.getRegistrationOpenDate().toString());
        ps.setString(7, c.getRegistrationCloseDate().toString());
        ps.setInt(8, c.getCapacity());
        ps.setString(9, c.getStatus().name());
    }

    private Conference mapRow(ResultSet rs) throws SQLException {
        Conference c = new Conference();
        c.setConferenceId(rs.getInt("conference_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setVenue(rs.getString("venue"));
        c.setStartDate(LocalDate.parse(rs.getString("start_date")));
        c.setEndDate(LocalDate.parse(rs.getString("end_date")));
        c.setRegistrationOpenDate(LocalDate.parse(rs.getString("registration_open_date")));
        c.setRegistrationCloseDate(LocalDate.parse(rs.getString("registration_close_date")));
        c.setCapacity(rs.getInt("capacity"));
        c.setStatus(ConferenceStatus.valueOf(rs.getString("status")));
        c.setCreatedAt(rs.getString("created_at"));
        return c;
    }
}