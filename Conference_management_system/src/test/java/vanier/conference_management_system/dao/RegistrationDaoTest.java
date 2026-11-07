package vanier.conference_management_system.dao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vanier.conference_management_system.database.DataAccessException;
import vanier.conference_management_system.database.Database;
import vanier.conference_management_system.model.Conference;
import vanier.conference_management_system.model.ConferenceStatus;
import vanier.conference_management_system.model.Registration;
import vanier.conference_management_system.model.RegistrationStatus;
import vanier.conference_management_system.model.Role;
import vanier.conference_management_system.model.User;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationDaoTest {

    @TempDir
    Path tempDir;

    private final RegistrationDao registrationDao = new RegistrationDao();
    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final UserDao userDao = new UserDao();

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

    private int newUser(String email) {
        return userDao.create(new User("Test", "User", email, null, null, Role.PARTICIPANT)).getUserId();
    }

    private int newConference(int capacity) {
        return conferenceDao.create(new Conference("Talk about pumping grades", "Deans Office", "Room D201",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                capacity, ConferenceStatus.OPEN)).getConferenceId();
    }

    @Test
    void createFindUpdateDelete() {
        int userId = newUser("dean@example.com");
        Registration r = registrationDao.create(new Registration(2, userId, RegistrationStatus.CONFIRMED, null));
        assertTrue(r.getRegistrationId() > 0);

        Registration found = registrationDao.findById(r.getRegistrationId()).orElseThrow();
        assertEquals(RegistrationStatus.CONFIRMED, found.getStatus());
        assertNull(found.getWaitingPosition());

        found.setStatus(RegistrationStatus.CANCELLED);
        assertTrue(registrationDao.update(found));
        assertEquals(RegistrationStatus.CANCELLED, registrationDao.findById(r.getRegistrationId()).orElseThrow().getStatus());
        assertTrue(registrationDao.delete(r.getRegistrationId()));
        assertTrue(registrationDao.findById(r.getRegistrationId()).isEmpty());
    }

    @Test
    void findMethodsReadSeedData() {
        assertEquals(5, registrationDao.findAll().size());
        assertEquals(3, registrationDao.findByConference(1).size());
        assertEquals(2, registrationDao.findByUser(2).size());

        List<Registration> waitingList = registrationDao.findWaitingList(1);
        assertEquals(2, waitingList.size());
        assertEquals(1, waitingList.get(0).getWaitingPosition());
        assertEquals(3, waitingList.get(0).getUserId());
    }

    @Test
    void sameUserCannotRegisterTwiceForSameConference() {
        assertThrows(DataAccessException.class, () -> registrationDao.create(new Registration(1, 2, RegistrationStatus.CONFIRMED, null)));
    }

    @Test
    void waitlistedRegistrationNeedsAPosition() {
        int userId = newUser("beatrice@example.com");
        assertThrows(DataAccessException.class, () -> registrationDao.create(new Registration(2, userId, RegistrationStatus.WAITLISTED, null)));
    }

    @Test
    void registerConfirmsWhenSeatsAreFree() {
        int conferenceId = newConference(2);
        Registration r = registrationDao.register(conferenceId, newUser("credo@example.com"));

        assertEquals(RegistrationStatus.CONFIRMED, r.getStatus());
        assertEquals(1, registrationDao.countConfirmed(conferenceId));
        assertEquals(ConferenceStatus.OPEN, conferenceDao.findById(conferenceId).orElseThrow().getStatus());
    }

    @Test
    void registerTakingLastSeatMarksConferenceFull() {
        int conferenceId = newConference(1);
        registrationDao.register(conferenceId, newUser("daksh@example.com"));

        assertEquals(ConferenceStatus.FULL, conferenceDao.findById(conferenceId).orElseThrow().getStatus());
    }

    @Test
    void registerWhenFullGoesToWaitingListInOrder() {
        int conferenceId = newConference(1);
        registrationDao.register(conferenceId, newUser("projectdeliverable@example.com"));

        Registration second = registrationDao.register(conferenceId, newUser("john@example.com"));
        Registration third = registrationDao.register(conferenceId, newUser("giuliano@example.com"));

        assertEquals(RegistrationStatus.WAITLISTED, second.getStatus());
        assertEquals(1, second.getWaitingPosition());
        assertEquals(2, third.getWaitingPosition());
    }

    @Test
    void registerForDraftConferenceIsRefused() {
        assertThrows(IllegalStateException.class, () -> registrationDao.register(3, newUser("heidi@example.com")));
        assertTrue(registrationDao.findByConference(3).isEmpty());
    }

    @Test
    void registerTwiceRollsBack() {
        int conferenceId = newConference(1);
        int userId = newUser("internety@example.com");
        registrationDao.register(conferenceId, userId);

        assertThrows(DataAccessException.class, () -> registrationDao.register(conferenceId, userId));
        assertEquals(1, registrationDao.findByConference(conferenceId).size());
    }
}