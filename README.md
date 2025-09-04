# 🏃‍♂️ Ferum Backend API

> API backend pour l'application de génération de plan d'entrainement Ferum, développée avec Quarkus et Java 21.

## 🚀 Démarrage rapide

### Prérequis

- **Java 21+** (OpenJDK recommandé)
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL 15+** (ou via Docker)

### Démarrage de l'application

```bash
# Cloner le projet
git clone <repository-url>
cd backend-api

# Lancer l'application en mode développement
./mvnw quarkus:dev

# Lancer les tests
./mvnw test
```

L'API sera accessible sur **http://localhost:8080**

## 🏗️ Architecture

### Structure du projet

```
src/main/java/org/heigvd/
├── dto/                     # Data Transfer Objects
├── entity/                  # Entités JPA
├── resource/                # REST Controllers
├── service/                 # Services métier
├── training_generator/      # Générateurs d'entraînement
└── workout_analyser/        # Analyseurs de performance
```

### Technologies principales

- **[Quarkus](https://quarkus.io/)** - Framework Java natif
- **JAX-RS (RESTEasy)** - API REST
- **JPA/Hibernate** - ORM
- **PostgreSQL** - Base de données
- **JWT** - Authentification
- **OpenAPI** - Documentation API

## 📚 API Documentation

La documentation interactive est disponible sur :
- **Swagger UI** : http://localhost:8080/api

### Endpoints principaux

```
POST   /auth/login              # Authentification
GET    /auth/me                 # Profil utilisateur
POST   /training-plan           # Créer un plan d'entraînement
GET    /workouts                # Séances d'entraînement
POST   /workouts                # Enregistrer une séance
GET    /goals                   # Objectifs disponibles
```

## 🔧 Fichiers de tests
> Etant donné qu'il est compliqué de pouvoir tester la réconciliation entre un entrainement planifié et un entrainement effectué, nous vous mettons à dispositions des workouts de tests avec des données fictives.
> 
> Ces fichiers sont situés dans le dossier `src/json`.

Pour utiliser ces fichiers il vous suffira de les copier-coller dans le body d'une requête `POST` sur `/workout` en utilisant par exemple un outil permettant d'exécuter des requêtes HTTP comme Postman, Insomnia, Curl, ou HTTPie.

⚠️ **Attention** : lors-ce que votre training plan est défini pour une semaine, il faut que la date du workout soit la même que la date de l'entrainement planifié par le plan d'entrainement. Pour cela, faite un CTRL + F pour chercher la date et remplacez la par la date de votre entrainement planifié.