package app.utils;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.*;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dtos.TokenDTO;
import app.entities.Role;
import app.entities.User;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;

public class RoutesTest {
    private static EntityManagerFactory emf;
    private static final int port = 7070;

    @BeforeAll
    public static void setUp() {
        emf = HibernateConfig.getEntityManagerFactory(true);
        RestAssured.baseURI = "http://localhost:" + port + "/api";

        // Start Server
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance(emf);
        Routes routes = Routes.getInstance(emf);
        applicationConfig
                .initiateServer()
                .startServer(port)
                .setExceptionHandling()
                .setRoute(routes.securityResources())
                .checkSecurityRoles();

        // Clear any leftovers
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @AfterAll
    public static void tearDown() {
        ApplicationConfig.getInstance().stopServer();
    }

    @BeforeEach
    public void setUpData() {
        // Insert data into the database
        User user = new User("user", "user");
        user.addRole(new Role("user"));
        User admin = new User("admin", "admin");
        admin.addRole(new Role("admin"));

        // Insert data into the database
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(user);
            em.persist(admin);
            em.getTransaction().commit();
        }

        // Print all data from database
        try (var em = emf.createEntityManager()) {
            em.createQuery("SELECT u FROM User u", User.class).getResultList().forEach(System.out::println);
            em.createQuery("SELECT r FROM Role r", Role.class).getResultList().forEach(System.out::println);
            // print out user_role db with the UserRoleDTO
           // em.createQuery("SELECT new app.dtos.UserRoleDTO(ur.username, ur.role) FROM User u JOIN u.roles ur", UserRoleDTO.class).getResultList().forEach(System.out::println);
        }
    }

    @AfterEach
    public void clearData() {
        // Clear data from the database
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            em.getTransaction().commit();
        }

        // Print all data from database
        try (var em = emf.createEntityManager()) {
            em.createQuery("SELECT u FROM User u", User.class).getResultList().forEach(System.out::println);
            em.createQuery("SELECT r FROM Role r", Role.class).getResultList().forEach(System.out::println);
        }
    }

    @Test
    public void testLoginAsUser() {
        // Test login
        String expectedUsername = "user";
        TokenDTO token = RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\""+expectedUsername+"\",\"password\":\"user\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(TokenDTO.class);

        String actualUsername= token.getUserName();

        assertEquals(actualUsername, actualUsername);
    }

    @Test
    public void testLoginAsAdmin() {
        // Test login
        String expectedUsername = "admin";
        TokenDTO token = RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\""+expectedUsername+"\",\"password\":\"admin\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(TokenDTO.class);

        String actualUsername= token.getUserName();

        assertEquals(actualUsername, actualUsername);
    }

    @Test
    public void testLoginWithWrongPassword() {
        // Test login
        String expectedUsername = "user";
        RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\""+expectedUsername+"\",\"password\":\"wrong\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }
}
