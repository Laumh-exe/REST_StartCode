package app.testUtils;

import app.entities.Role;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class TestUtil {
    public static void createUsers(EntityManagerFactory emfTest) {
        // Clear any leftovers
        try (EntityManager em = emfTest.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.getTransaction().commit();

            // Insert data into the database
            User u1 = new User("user", "user");
            User u2 = new User("admin", "admin");

            Role r1 = new Role("admin");
            Role r2 = new Role("user");
            Role r3 = new Role("unasigned");

            u2.addRole(r1);
            u1.addRole(r2);

            em.getTransaction().begin();
            em.persist(r1);
            em.persist(r2);
            em.persist(r3);
            em.persist(u1);
            em.persist(u2);
            em.getTransaction().commit();
        }
    }
}
