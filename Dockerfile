# Étape 1 : Phase de construction
FROM eclipse-temurin:21-jdk-focal AS build

RUN apt-get update && apt-get install -y maven
WORKDIR /app

# Optimisation du cache Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean install -DskipTests

# Étape 2 : Phase d'exécution
FROM eclipse-temurin:21-jre-focal

WORKDIR /app
EXPOSE 8080

# Utilisation d'un wildcard pour copier le JAR sans se soucier de la version exacte
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]