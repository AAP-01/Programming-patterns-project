package vanier.conference_management_system.dao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vanier.conference_management_system.database.DataAccessException;
import vanier.conference_management_system.database.Database;
import vanier.conference_management_system.model.Conference;
import vanier.conference_management_system.model.ConferenceStatus;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ConferenceDaoTest {

    @TempDir
    Path tempDir;

    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final RegistrationDao registrationDao = new RegistrationDao();

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

    private Conference sampleConference() {
        return new Conference("Teacher Conference Meeting About Tests", "All about making tests easier for students", "Room D243",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 2),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 25),
                30, ConferenceStatus.OPEN);
    }

    @Test
    void createAndFindConference() {
        Conference created = conferenceDao.create(sampleConference());
        assertTrue(created.getConferenceId() > 0);

        Conference found = conferenceDao.findById(created.getConferenceId()).orElseThrow();
        assertEquals("Teacher Conference Meeting About Tests", found.getTitle());
        assertEquals(LocalDate.of(2026, 12, 1), found.getStartDate());
        assertEquals(30, found.getCapacity());
    }

    @Test
    void findAllAndFindByStatus() {
        assertEquals(3, conferenceDao.findAll().size());
        assertEquals(2, conferenceDao.findByStatus(ConferenceStatus.OPEN).size());
        assertEquals(1, conferenceDao.findByStatus(ConferenceStatus.DRAFT).size());
    }

    @Test
    void updateConference() {
        Conference d = conferenceDao.findById(1).orElseThrow();
        d.setVenue("D Building");
        d.setCapacity(100);
        assertTrue(conferenceDao.update(d));

        Conference updated = conferenceDao.findById(1).orElseThrow();
        assertEquals("D Building", updated.getVenue());
        assertEquals(100, updated.getCapacity());
    }

    @Test
    void updateStatus() {
        assertTrue(conferenceDao.updateStatus(1, ConferenceStatus.OPEN));
        assertEquals(ConferenceStatus.OPEN, conferenceDao.findById(1).orElseThrow().getStatus());
    }

    @Test
    void deleteConferenceAlsoDeletesItsRegistrations() {
        assertFalse(registrationDao.findByConference(2).isEmpty());
        assertTrue(conferenceDao.delete(2));
        assertTrue(conferenceDao.findById(2).isEmpty());
        assertTrue(registrationDao.findByConference(2).isEmpty());
    }

    @Test
    void capacityMustBePositive() {
        Conference bad = sampleConference();
        bad.setCapacity(0);
        assertThrows(DataAccessException.class, () -> conferenceDao.create(bad));
    }

    @Test
    void endDateCannotBeBeforeStartDate() {
        Conference bad = sampleConference();
        bad.setEndDate(LocalDate.of(2026, 11, 30));
        assertThrows(DataAccessException.class, () -> conferenceDao.create(bad));
    }
}