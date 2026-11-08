package vanier.conference_management_system.dao;

import vanier.conference_management_system.database.DataAccessException;
import vanier.conference_management_system.database.Database;
import vanier.conference_management_system.model.Credential;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CredentialDao {

    private static final Logger LOGGER = Logger.getLogger(CredentialDao.class.getName());

    private static final String INSERT = "INSERT INTO credentials (user_id, username, password) VALUES (?, ?, ?)";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM credentials WHERE user_id = ?";
    private static final String SELECT_BY_USERNAME = "SELECT * FROM credentials WHERE username = ?";
    private static final String SELECT_ALL = "SELECT * FROM credentials ORDER BY user_id";
    private static final String UPDATE = "UPDATE credentials SET username = ?, password = ? WHERE user_id = ?";
    private static final String DELETE = "DELETE FROM credentials WHERE user_id = ?";

    public Credential create(Credential credential) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setInt(1, credential.getUserId());
            ps.setString(2, credential.getUsername());
            ps.setString(3, credential.getPassword());
            ps.executeUpdate();
            return credential;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not create credentials for user " + credential.getUserId(), e);
            throw new DataAccessException("Could not create credentials", e);
        }
    }

    public Optional<Credential> findByUserId(int userId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_USER_ID)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not find credentials for user " + userId, e);
            throw new DataAccessException("Could not find credentials", e);
        }
    }

    public Optional<Credential> findByUsername(String username) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not find credentials for username " + username, e);
            throw new DataAccessException("Could not find credentials", e);
        }
    }

    public List<Credential> findAll() {
        List<Credential> credentials = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                credentials.add(mapRow(rs));
            }
            return credentials;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not list credentials", e);
            throw new DataAccessException("Could not list credentials", e);
        }
    }

    public boolean update(Credential credential) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setString(1, credential.getUsername());
            ps.setString(2, credential.getPassword());
            ps.setInt(3, credential.getUserId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not update credentials for user " + credential.getUserId(), e);
            throw new DataAccessException("Could not update credentials", e);
        }
    }

    public boolean delete(int userId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not delete credentials for user " + userId, e);
            throw new DataAccessException("Could not delete credentials", e);
        }
    }

    private Credential mapRow(ResultSet rs) throws SQLException {
        return new Credential(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"));
    }
}