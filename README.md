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

## 🔧 Fichiers de test pour les entraînements

Pour faciliter les tests de réconciliation entre entraînements planifiés et entraînements réalisés, des données d'exemple sont disponibles dans le dossier `src/json`.

### Utilisation

Ces fichiers peuvent être utilisés directement comme payload dans une requête `POST` vers l'endpoint `/workout` avec des outils comme Postman, Insomnia, curl ou HTTPie.

### Synchronisation des dates

**Important** : Les dates des entraînements de test doivent correspondre aux dates planifiées dans votre plan d'entraînement.

Pour ajuster les dates :
1. Recherchez les champs de date dans le fichier JSON (généralement `start` et `end`, ainsi que toutes les timestamps BPM et SPEED)
2. Remplacez-les par la date correspondant à votre entraînement planifié
3. Vérifiez que le sport correspond également (`Running`, `Cycling`, ou `Swimming`)

Cette synchronisation permet de tester correctement la fusion entre les données planifiées et les données réellement enregistrées.