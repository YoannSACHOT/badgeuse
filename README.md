# Badgeuse

Badgeuse est une application réactive développée avec Spring Boot 3 et Java 21. Elle permet de gérer un système de badgeage pour les employés, incluant les opérations CRUD sur les employés ainsi que l'enregistrement des traces de badgeages. L'application calcule le temps passé par jour et par mois par chaque employé, en se basant sur une durée de travail de 7 heures par jour. Elle utilise MongoDB en base de données, déployé dans un container Docker, et intègre la gestion des exceptions et la validation des données.

## Table des matières
- [Prérequis](#prerequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Endpoints de l'API](#endpoints-de-lapi)
- [Tests](#tests)
- [CI/CD avec GitHub Actions](#ci-cd-avec-github-actions)
- [Sécurité](#securite)
- [License](#license)

## Prérequis

### Java 21
Assurez-vous d'avoir installé JDK 21.

### Maven
Pour la gestion des dépendances et la compilation du projet.

### Docker
Pour lancer MongoDB dans un container.

## Installation

### Cloner le repository :
```bash
git clone https://github.com/votre-utilisateur/yoannsachot-badgeuse.git
cd yoannsachot-badgeuse
```

### Lancer MongoDB avec Docker Compose :
Le projet inclut un fichier `docker-compose.yml` qui permet de démarrer une instance MongoDB :
```bash
docker-compose up -d
```
Cela démarre un container MongoDB accessible sur le port `27017` avec une base de données nommée `badgeuse`.

### Configurer l'application :
Le fichier de configuration `src/main/resources/application.yml` pointe par défaut vers `mongodb://localhost:27017/badgeuse`. Vous pouvez modifier ce fichier si nécessaire.

## Configuration

### Pom.xml
Le projet utilise Spring Boot 3 avec les starters suivants :
- `spring-boot-starter-data-mongodb-reactive`
- `spring-boot-starter-webflux`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`

Pour les tests, les dépendances incluent :
- `spring-boot-starter-test`
- `reactor-test`
- `spring-security-test`
- `mockito-inline`

## Utilisation

### Compiler et lancer l'application :
Vous pouvez démarrer l'application en utilisant Maven :
```bash
mvn spring-boot:run
```
Ou bien construire le jar exécutable :
```bash
mvn clean package
java -jar target/badgeuse-0.0.1-SNAPSHOT.jar
```

### Accéder aux endpoints de l'API :
L'application expose une API REST réactive sur le port par défaut (`8080`).

## Endpoints de l'API

### Gestion des employés
- **POST** `/api/employees` : Créer un nouvel employé.  
  *Corps de la requête* : JSON avec `name` et `email`.
- **GET** `/api/employees/{id}` : Récupérer les informations d’un employé par son ID.
- **PUT** `/api/employees/{id}` : Mettre à jour un employé existant.
- **DELETE** `/api/employees/{id}` : Supprimer un employé.
- **GET** `/api/employees` : Lister tous les employés.

### Gestion des traces de badgeage
- **POST** `/api/badges/employee/{employeeId}` : Ajouter une trace de badgeage (badge IN/OUT).  
  *Corps de la requête* : JSON avec `timestamp` et `type`.
- **GET** `/api/badges/employee/{employeeId}/day/{date}` : Obtenir le rapport de temps pour une journée (*format date : "yyyy-MM-dd"*).
- **GET** `/api/badges/employee/{employeeId}/month/{month}` : Obtenir le rapport de temps pour un mois (*format mois : "yyyy-MM"*).

## Tests
Le projet intègre des tests unitaires et d'intégration. Pour lancer les tests, utilisez la commande :
```bash
mvn clean verify
```
Le fichier `BadgeuseApplicationTests.java` vérifie le chargement du contexte. Pensez à ajouter des tests complémentaires pour vos services et contrôleurs.

## CI/CD avec GitHub Actions
Un workflow GitHub Actions est présent dans le fichier `.github/workflows/clean-verify.yml`. Ce workflow :
- Démarre un container MongoDB pour les tests.
- Configure JDK 21.
- Met en cache les dépendances Maven.
- Exécute la commande `mvn clean verify`.

Ce workflow assure que chaque push ou pull request sur la branche `main` déclenche la compilation et les tests de l’application.

## Sécurité
Une configuration de sécurité par défaut est active via Spring Security. Pour modifier ou personnaliser cette configuration, créez ou adaptez la classe `SecurityConfig` dans le package `fr.jixter.badgeuse.config`. Par exemple :

```java
package fr.jixter.badgeuse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }
}
```

## License
Ce projet est sous licence MIT. Voir le fichier `LICENSE.md` pour plus de détails.

