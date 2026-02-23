# Utilisation d'une image JRE légère pour Java 21
FROM eclipse-temurin:21-jre-jammy

# Dossier de travail dans le container
WORKDIR /app

# Copie le JAR généré par Maven (dans le dossier target/) vers le container
COPY target/*.jar app.jar

# Port exposé par l'API
EXPOSE 8080

# Commande de lancement
ENTRYPOINT ["java", "-jar", "app.jar"]