package app.utils;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.*;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dtos.TokenDTO;
import app.entities.Role;
import app.entities.User;
import app.testUtils.TestUtil;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import jakarta.persistence.EntityManagerFactory;

public class RoutesTest {
    private static HibernateConfig hibernateConfig = new HibernateConfig();
    private static EntityManagerFactory emf;
    private static final int port = 7070;

    @BeforeAll
    public static void setUp() {
        emf = hibernateConfig.getEntityManagerFactory(true);
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
    public void testLoginAsUser() {
        // Test login
        RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\"user\",\"password\":\"user\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(TokenDTO.class);
    }

    @Test
    public void testLoginAsAdmin() {
        // Test login
        RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"admin\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(TokenDTO.class);

    }

    @Test
    public void testLoginWithWrongPassword() {
        // Test login
        RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\"user\",\"password\":\"wrong\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    
    @Test
    public void testAddRoleToUser() {
        TokenDTO token = RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"admin\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(TokenDTO.class);

                Header header = new Header("Authorization", "Bearer " + token.getToken());
        // Test add role to user
        RestAssured
                .given()
                .contentType("application/json")
                .header(header)
                .body("{\"username\":\"user\",\"role\":\"unasigned\"}")
                .when()
                .post("/auth/addRoleToUser")
                .then()
                .statusCode(200);
    }

    @Test
    public void testAddRoleToUserAdminNotLoggedIn() {
        // Test add role to user
        RestAssured
                .given()
                .contentType("application/json")
                .body("{\"username\":\"user\",\"role\":\"unasigned\"}")
                .when()
                .post("/auth/addRole")
                .then()
                .statusCode(404);
    }
}
