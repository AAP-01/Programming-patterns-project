package vanier.conference_management_system.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vanier.conference_management_system.database.DataAccessException;
import vanier.conference_management_system.database.Database;
import vanier.conference_management_system.model.Credential;
import vanier.conference_management_system.model.Role;
import vanier.conference_management_system.model.User;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    @TempDir
    Path tempDir;

    private final UserDao userDao = new UserDao();
    private final CredentialDao credentialDao = new CredentialDao();

    @BeforeEach
    void setUp() {
        Database.setUrl("jdbc:sqlite:" + tempDir.resolve("test.db"));
        Database.createTables();
        Database.insertSeedData();
    }

    @AfterEach
    void tearDown() {
        Database.resetUrl();
    }

    @Test
    void foreignKeysAreOnForEveryConnection() throws Exception {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA foreign_keys")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void seedDataContainsTheAdministrator() {
        User admin = userDao.findById(1).orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("admin", credentialDao.findByUserId(1).orElseThrow().getUsername());
    }

    @Test
    void createAndFindUser() {
        User user = userDao.create(new User("Beatrice", "Lopez", "beatrice@example.com", null, "Vanier", Role.PARTICIPANT));
        assertTrue(user.getUserId() > 0);
        User found = userDao.findById(user.getUserId()).orElseThrow();
        assertEquals("Beatrice", found.getFirstName());
        assertEquals("beatrice@example.com", found.getEmail());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    void findAllReturnsSeedUsers() {
        assertEquals(4, userDao.findAll().size());
    }

    @Test
    void updateUser() {
        User user = userDao.findByEmail("credo.dotse@example.com").orElseThrow();
        user.setOrganization("Vanier");
        assertTrue(userDao.update(user));
        assertEquals("Vanier", userDao.findById(user.getUserId()).orElseThrow().getOrganization());
    }

    @Test
    void deleteUserAlsoDeletesCredentials() {
        assertTrue(userDao.delete(2));
        assertTrue(userDao.findById(2).isEmpty());
        assertTrue(credentialDao.findByUserId(2).isEmpty());
    }

    @Test
    void duplicateEmailIsRefused() {
        User copy = new User("Giuliano", "Di Fulvio", "admin@conference.local", null, null, Role.PARTICIPANT);
        assertThrows(DataAccessException.class, () -> userDao.create(copy));
    }

    @Test
    void createWithCredentialsSavesBothTables() {
        User user = userDao.createWithCredentials(new User("Bruce", "Lee", "brucelee@example.com", null, null, Role.PARTICIPANT), "bruce", "adminpass");
        assertTrue(userDao.findById(user.getUserId()).isPresent());
        Credential credential = credentialDao.findByUsername("bruce").orElseThrow();
        assertEquals(user.getUserId(), credential.getUserId());
    }

    @Test
    void createWithCredentialsRollsBackWhenUsernameIsTaken() {
        User user = new User("Bruce", "Lee", "brucelee@example.com", null, null, Role.PARTICIPANT);
        assertThrows(DataAccessException.class, () -> userDao.createWithCredentials(user, "admin", "adminpass"));
        assertTrue(userDao.findByEmail("brucelee@example.com").isEmpty());
    }

    @Test
    void credentialCrud() {
        User user = userDao.create(new User("Alex", "Vilvert", "vilvalex@example.com", null, null, Role.PARTICIPANT));
        credentialDao.create(new Credential(user.getUserId(), "AlexStein", "password"));
        assertEquals("password", credentialDao.findByUsername("AlexStein").orElseThrow().getPasswordHash());

        assertTrue(credentialDao.update(new Credential(user.getUserId(), "Mohand", "password1")));
        assertEquals("password1", credentialDao.findByUserId(user.getUserId()).orElseThrow().getPasswordHash());

        assertTrue(credentialDao.delete(user.getUserId()));
        assertTrue(credentialDao.findByUsername("Alex").isEmpty());
    }

    @Test
    void credentialsForUnknownUserAreRefused() {
        assertThrows(DataAccessException.class, () -> credentialDao.create(new Credential(999, "Giuliano", "passwordgiuliano")));
    }
}