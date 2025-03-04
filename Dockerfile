# Étape 1 : Construction de l'application avec Maven et JDK 21
FROM maven:3.9.9-eclipse-temurin AS builder
WORKDIR /app
# Copier le fichier pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copier le reste du code source
COPY src ./src
# Compiler l'application et construire le jar sans lancer les tests
RUN mvn clean package -DskipTests

# Étape 2 : Création de l'image finale avec OpenJDK 21
FROM openjdk:21-slim
WORKDIR /app
# Copier le jar construit depuis l'étape builder
COPY --from=builder /app/target/badgeuse-0.0.1-SNAPSHOT.jar app.jar
# Exposer le port utilisé par l'application
EXPOSE 8080
# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
