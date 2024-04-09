package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import app.utils.Routes;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    private static final int port = 7170;
    private static HibernateConfig hibernateConfig = new HibernateConfig();

    public static void main(String[] args) throws EntityNotFoundException {
        // JAVALIN SETUP
        EntityManagerFactory emf = hibernateConfig.getEntityManagerFactory(false);
        startServer(emf);
        //closeServer();

        User user = new User("user", "user");
        User admin = new User("admin", "admin");
        Role r1 = new Role("admin");
        Role r2 = new Role("user");
        admin.addRole(r1);
        user.addRole(r2);
        try(var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(r1);
            em.persist(r2);
            em.persist(user);
            em.persist(admin);
            em.getTransaction().commit();
        }
    }

    public static void startServer(EntityManagerFactory emf) {
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance(emf);
        Routes routes = Routes.getInstance(emf);
        applicationConfig
                .initiateServer()
                .startServer(port)
                .setExceptionHandling()
                .setRoute(routes.securityResources())
                .setRoute(routes.testResources())
                //Add more endpoints here 
                .checkSecurityRoles();
    }

    public static void closeServer() {
        ApplicationConfig.getInstance().stopServer();
    }
}
