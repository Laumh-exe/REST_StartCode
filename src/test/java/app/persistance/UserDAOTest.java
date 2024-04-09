package app.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import app.testUtils.TestUtil;
import app.utils.Routes;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class UserDAOTest {
    private static HibernateConfig hibernateConfig = new HibernateConfig();
    private static EntityManagerFactory emf;
    private static final int port = 7070;
    private static UserDAO userDAO;

    @BeforeAll
    public static void setUp() {
        emf = hibernateConfig.getEntityManagerFactory(true);
        RestAssured.baseURI = "http://localhost:" + port + "/api";
        userDAO = UserDAO.getUserDAOInstance(emf);

        // Start Server
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance(emf);
        Routes routes = Routes.getInstance(emf);
        applicationConfig
                .initiateServer()
                .startServer(port)
                .setExceptionHandling()
                .setRoute(routes.securityResources())
                .checkSecurityRoles();
    }

    @AfterAll
    public static void tearDown() {
        ApplicationConfig.getInstance().stopServer();
    }

    @BeforeEach
    public void setUpData() {
        TestUtil.createUsers(emf);
    }

    @Test
    void testAddRoleToUser() throws EntityNotFoundException {
        try (EntityManager em = emf.createEntityManager()) {
            userDAO.addRoleToUser("user", "unasigned");
            User user = em.find(User.class, "user");
            Role role = em.find(Role.class, "unasigned");
            assertTrue(user.getRoles().contains(role));
            assertTrue(role.getUsers().contains(user));
        }
    }

    @Test
    void testCreateRole() {
        try (EntityManager em = emf.createEntityManager()) {
            userDAO.createRole("testRole");
            Role role = em.find(Role.class, "testRole");
            assertTrue(role != null);
            assertTrue(role.getName().equals("testRole"));
        }
    }

    @Test
    void testCreateUser() {
        try (EntityManager em = emf.createEntityManager()) {
            userDAO.createUser("testUser", "testPassword");
            User userFromDB = em.find(User.class, "testUser");
            assertTrue(userFromDB != null);
            assertTrue(userFromDB.getUsername().equals("testUser"));
        }
    }
    @Test
    void testCreateUserPasswordHashed() {
        try (EntityManager em = emf.createEntityManager()) {
            userDAO.createUser("testUser", "testPassword");
            User userFromDB = em.find(User.class, "testUser");
            assertTrue(userFromDB != null);
            assertTrue(userFromDB.getUsername().equals("testUser"));
            assertFalse(userFromDB.getPassword().equals("testPassword"));
        }
    }

    @Test
    void testVerifyUserPostive() throws EntityNotFoundException {
        try (EntityManager em = emf.createEntityManager()) {
            User user = userDAO.verifyUser("user", "user");
            assertTrue(user != null);
        }
    }

    @Test
    void testVerifyUserNegative() throws EntityNotFoundException {
        String username = "userDOESNOTEXIST";
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userDAO.verifyUser(username, "user");
        });
        assertEquals("No user found with username: " + username, exception.getMessage());
    }
}
