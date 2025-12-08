# Étape 1 : Phase de construction (pour compiler l'application)
FROM eclipse-temurin:17-jdk-focal AS build

# Mettre à jour et installer maven
RUN apt-get update && apt-get install -y maven

# Définir le répertoire de travail
WORKDIR /app

# Copier le pom.xml pour télécharger les dépendances
COPY pom.xml .

# Télécharger les dépendances
RUN mvn dependency:go-offline -B

# Copier le reste du code source
COPY src ./src

# Compiler et empaqueter l'application (génère le JAR)
RUN mvn clean install -DskipTests

# Étape 2 : Phase d'exécution (pour minimiser la taille de l'image finale)
FROM eclipse-temurin:17-jre-focal

# Définir le répertoire de travail
WORKDIR /app

# Exposer le port de l'API (8080 par défaut dans Spring Boot)
EXPOSE 8080

# Copier le JAR compilé de l'étape de construction
# Assurez-vous que le nom du JAR correspond à celui généré par Maven
COPY --from=build /app/target/4aWalk-api-0.0.1-SNAPSHOT.jar app.jar

# Commande d'exécution de l'application
ENTRYPOINT ["java", "-jar", "app.jar"]