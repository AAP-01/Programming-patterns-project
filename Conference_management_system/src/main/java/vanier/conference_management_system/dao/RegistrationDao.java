package vanier.conference_management_system.dao;

import vanier.conference_management_system.database.DataAccessException;
import vanier.conference_management_system.database.Database;
import vanier.conference_management_system.model.ConferenceStatus;
import vanier.conference_management_system.model.Registration;
import vanier.conference_management_system.model.RegistrationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistrationDao {

    private static final String COLUMNS = "registration_id, conference_id, user_id, status, waiting_position, registered_at";

    public Registration create(Registration r) {
        try (Connection c = Database.getConnection()) {
            return insert(c, r);
        } catch (SQLException e) {
            throw new DataAccessException("Could not create registration", e);
        }
    }

    public Optional<Registration> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM registrations WHERE registration_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not find registration " + id, e);
        }
    }

    public List<Registration> findAll() {
        return query("SELECT " + COLUMNS + " FROM registrations ORDER BY registration_id");
    }

    public List<Registration> findByConference(int conferenceId) {
        return query("SELECT " + COLUMNS + " FROM registrations WHERE conference_id = ? ORDER BY registration_id", conferenceId);
    }

    public List<Registration> findByUser(int userId) {
        return query("SELECT " + COLUMNS + " FROM registrations WHERE user_id = ? ORDER BY registration_id", userId);
    }

    public List<Registration> findWaitingList(int conferenceId) {
        return query("SELECT " + COLUMNS + " FROM registrations " + "WHERE conference_id = ? AND status = 'WAITLISTED' ORDER BY waiting_position", conferenceId);
    }

    public int countConfirmed(int conferenceId) {
        try (Connection c = Database.getConnection()) {
            return countConfirmed(c, conferenceId);
        } catch (SQLException e) {
            throw new DataAccessException("Could not count confirmed registrations", e);
        }
    }

    public boolean update(Registration r) {
        String sql = "UPDATE registrations SET conference_id = ?, user_id = ?, status = ?, "
                + "waiting_position = ? WHERE registration_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getConferenceId());
            ps.setInt(2, r.getUserId());
            ps.setString(3, r.getStatus().name());
            setNullableInt(ps, 4, r.getWaitingPosition());
            ps.setInt(5, r.getRegistrationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update registration", e);
        }
    }

    public boolean delete(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM registrations WHERE registration_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete registration " + id, e);
        }
    }

    public Registration register(int conferenceId, int userId) {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String status;
                int capacity;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status, capacity FROM conferences WHERE conference_id = ?")) {
                    ps.setInt(1, conferenceId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Conference " + conferenceId + " does not exist");
                        }
                        status = rs.getString("status");
                        capacity = rs.getInt("capacity");
                    }
                }
                ConferenceStatus conferenceStatus = ConferenceStatus.valueOf(status);
                if (conferenceStatus != ConferenceStatus.OPEN && conferenceStatus != ConferenceStatus.FULL) {
                    throw new IllegalStateException("Conference is not open for registration: " + conferenceStatus);
                }

                int confirmed = countConfirmed(c, conferenceId);
                Registration result;
                if (confirmed < capacity) {
                    result = insert(c, new Registration(conferenceId, userId, RegistrationStatus.CONFIRMED, null));
                    if (confirmed + 1 >= capacity) {
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE conferences SET status = 'FULL' WHERE conference_id = ?")) {
                            ps.setInt(1, conferenceId);
                            ps.executeUpdate();
                        }
                    }
                } else {
                    int nextPosition;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COALESCE(MAX(waiting_position), 0) + 1 FROM registrations "
                                    + "WHERE conference_id = ? AND status = 'WAITLISTED'")) {
                        ps.setInt(1, conferenceId);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            nextPosition = rs.getInt(1);
                        }
                    }
                    result = insert(c, new Registration(conferenceId, userId,
                            RegistrationStatus.WAITLISTED, nextPosition));
                }
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (e instanceof SQLException se) {
                    throw new DataAccessException("Could not register user " + userId, se);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not register user " + userId, e);
        }
    }

    private Registration insert(Connection c, Registration r) throws SQLException {
        String sql = "INSERT INTO registrations (conference_id, user_id, status, waiting_position) " + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getConferenceId());
            ps.setInt(2, r.getUserId());
            ps.setString(3, r.getStatus().name());
            setNullableInt(ps, 4, r.getWaitingPosition());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setRegistrationId(keys.getInt(1));
                }
            }
            return r;
        }
    }

    private int countConfirmed(Connection c, int conferenceId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM registrations WHERE conference_id = ? AND status = 'CONFIRMED'")) {
            ps.setInt(1, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<Registration> query(String sql, Object... params) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Registration> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read registrations", e);
        }
    }

    private Registration map(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setRegistrationId(rs.getInt("registration_id"));
        r.setConferenceId(rs.getInt("conference_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setStatus(RegistrationStatus.valueOf(rs.getString("status")));
        int pos = rs.getInt("waiting_position");
        r.setWaitingPosition(rs.wasNull() ? null : pos);
        r.setRegisteredAt(rs.getString("registered_at"));
        return r;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}