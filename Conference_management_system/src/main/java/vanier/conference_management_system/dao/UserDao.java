package vanier.conference_management_system.dao;

import vanier.conference_management_system.database.DataAccessException;
import vanier.conference_management_system.database.Database;
import vanier.conference_management_system.model.Role;
import vanier.conference_management_system.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    private static final String INSERT = "INSERT INTO users (first_name, last_name, email, phone, organization, role) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String SELECT_ALL = "SELECT * FROM users ORDER BY user_id";
    private static final String UPDATE = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, organization = ?, role = ? WHERE user_id = ?";
    private static final String DELETE = "DELETE FROM users WHERE user_id = ?";
    private static final String INSERT_CREDENTIAL = "INSERT INTO credentials (user_id, username, password) VALUES (?, ?, ?)";

    public User create(User user) {
        try (Connection connection = Database.getConnection()) {
            insertUser(connection, user);
            return user;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not create user " + user.getEmail(), e);
            throw new DataAccessException("Could not create user", e);
        }
    }

    public User createWithCredentials(User user, String username, String password) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertUser(connection, user);
                try (PreparedStatement ps = connection.prepareStatement(INSERT_CREDENTIAL)) {
                    ps.setInt(1, user.getUserId());
                    ps.setString(2, username);
                    ps.setString(3, password);
                    ps.executeUpdate();
                }
                connection.commit();
                return user;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not create user with credentials " + username, e);
            throw new DataAccessException("Could not create user with credentials", e);
        }
    }

    public Optional<User> findById(int userId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not find user " + userId, e);
            throw new DataAccessException("Could not find user", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not find user by email " + email, e);
            throw new DataAccessException("Could not find user", e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not list users", e);
            throw new DataAccessException("Could not list users", e);
        }
    }

    public boolean update(User user) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getOrganization());
            ps.setString(6, user.getRole().name());
            ps.setInt(7, user.getUserId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not update user " + user.getUserId(), e);
            throw new DataAccessException("Could not update user", e);
        }
    }

    public boolean delete(int userId) {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not delete user " + userId, e);
            throw new DataAccessException("Could not delete user", e);
        }
    }

    private void insertUser(Connection connection, User user) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getOrganization());
            ps.setString(6, user.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setOrganization(rs.getString("organization"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getString("created_at"));
        return user;
    }
}