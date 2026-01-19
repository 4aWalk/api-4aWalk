package iut.rodez.projet.sae.fourawalkapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
/** * Configuration pour MySQL (JPA)
 * Indique où se trouvent les interfaces Repository et les classes @Entity
 */
@EnableJpaRepositories(basePackages = "iut.rodez.projet.sae.fourawalkapi.repository.mysql")
@EntityScan(basePackages = "iut.rodez.projet.sae.fourawalkapi.entity")

/** * Configuration pour MongoDB
 * Indique où se trouvent les interfaces Repository et les classes @Document
 */
@EnableMongoRepositories(basePackages = "iut.rodez.projet.sae.fourawalkapi.repository.mongo")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}