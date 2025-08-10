# RecipeHub Backend

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Deployed%20on-Render-green.svg)](https://render.com/)

A robust Spring Boot REST API for recipe management with user authentication, recipe interactions, and social features. RecipeHub allows users to create, share, discover, and manage recipes with advanced search capabilities and recipe book organization.

## 🚀 Features

- **User Management**: Registration, authentication, profile management, and soft delete
- **Recipe Management**: Create, update, delete, fork recipes with image upload support
- **Recipe Books**: Organize recipes into public/private collections
- **Advanced Search**: Multi-criteria search with filtering (title, tags, author, cuisine, difficulty, etc.)
- **Smart Author Search**: Search by authorId returns both recipes and recipe books
- **Social Features**: Like recipes, mark as cooked/favorite, recipe forking
- **Image Upload**: AWS S3 integration for recipe images
- **Tagging System**: Categorize recipes with multiple tags (Easy, Quick, Italian, etc.)
- **Soft Delete**: Safe deletion with data preservation for users and recipes
- **Public/Private Recipes**: Control recipe visibility
- **Case-Insensitive Search**: User-friendly search experience across all text fields
- **Health Monitoring**: Built-in health check endpoints

## 🛠️ Technology Stack

- **Backend Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Database**: PostgreSQL (Production/Render), H2 (Testing)
- **Build Tool**: Maven
- **Security**: Spring Security with CSRF protection
- **Cloud Storage**: AWS S3 for image storage
- **Deployment**: Render.com with Docker
- **Testing**: JUnit 5, Spring Boot Test, Integration Tests
- **API Documentation**: Comprehensive REST API with detailed documentation

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher (for production)
- AWS Account (for S3 image storage)
- Docker (for containerized deployment)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/wendyww9/back-end-recipe-hub.git
cd back-end-recipe-hub
```

### 2. Development Setup

```bash
# Run tests to ensure everything works
./mvnw test

# Start development server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will be available at `http://localhost:8080`

### 3. Production Setup

```bash
# Build the application
./mvnw clean package -DskipTests

# Run with production profile
java -jar target/back-end-recipe-hub-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## ⚙️ Configuration

### Environment Variables

#### Development
```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/recipehub_dev
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
AWS_S3_BUCKET_NAME=your-s3-bucket
AWS_REGION=us-east-2
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

#### Production/Render
```bash
SPRING_PROFILES_ACTIVE=render
JDBC_DATABASE_URL=jdbc:postgresql://host:port/database
JDBC_DATABASE_USERNAME=your_username
JDBC_DATABASE_PASSWORD=your_password
AWS_S3_BUCKET_NAME=your-s3-bucket
AWS_REGION=us-east-2
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
PORT=8080
```

### Database Setup

1. Create a PostgreSQL database
2. Update the database connection properties in `application-dev.properties` or `application-render.properties`
3. The application will automatically create tables on startup

## 📚 API Documentation

### Quick API Overview

- **Authentication**: `POST /api/auth/register`, `POST /api/auth/login`
- **Users**: `GET /api/users`, `GET /api/users/{id}`, `DELETE /api/users/{id}`, `PUT /api/users/{id}`
- **Recipes**: `GET /api/recipes`, `POST /api/recipes`, `PUT /api/recipes/{id}`, `DELETE /api/recipes/{id}`, `POST /api/recipes/{id}/fork`
- **Search**: `GET /api/recipes/search` (with multiple filter options: title, tags, author, authorId, cuisine, difficulty, etc.)
- **Recipe Books**: `GET /api/recipebooks`, `POST /api/recipebooks`, `PUT /api/recipebooks/{id}`, `DELETE /api/recipebooks/{id}`
- **Tags**: `GET /api/tags`, `GET /api/tags/popular`, `GET /api/tags/categories`
- **Recipe Interactions**: `PUT /api/recipes/{id}/likecount`, `PUT /api/recipes/{id}/cooked`, `PUT /api/recipes/{id}/favourite`

### Complete API Documentation

For detailed API documentation including all endpoints, request/response formats, and examples, see:

📖 **[API Documentation](API_DOCUMENTATION.md)**

## 🧪 Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Classes
```bash
./mvnw test -Dtest=RecipeControllerIntegrationTest
./mvnw test -Dtest=UserControllerIntegrationTest
./mvnw test -Dtest=RecipeBookControllerIntegrationTest
./mvnw test -Dtest=TagControllerIntegrationTest
```

### Test Coverage
The project includes comprehensive integration tests covering:
- User management operations (create, update, delete, soft delete)
- Recipe CRUD operations (create, read, update, delete, fork)
- Advanced search functionality (multi-criteria filtering)
- Recipe book management (create, update, delete)
- Tag management and popular tags
- Error handling and validation
- Authentication and authorization

## 🚀 Deployment

### Render.com (Current Production)

The application is deployed on Render.com using Docker containers.

**Production URL**: https://back-end-recipe-hub.onrender.com

#### Render Deployment Features:
- **Automatic Deployment**: Connected to GitHub repository
- **Docker Containerization**: Multi-stage Docker build for optimized deployment
- **PostgreSQL Database**: Managed PostgreSQL service
- **Environment Variables**: Secure configuration management
- **Health Checks**: Built-in health monitoring endpoints
- **Auto-scaling**: Automatic scaling based on traffic

#### Render Environment Setup:
1. Connect GitHub repository to Render
2. Configure environment variables in Render dashboard
3. Set build command: `mvn clean package -DskipTests`
4. Set start command: `java -jar target/back-end-recipe-hub-0.0.1-SNAPSHOT.jar`

### Local Docker Deployment

```bash
# Build Docker image
docker build -t recipehub-backend .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=render \
  -e JDBC_DATABASE_URL=your_db_url \
  -e AWS_S3_BUCKET_NAME=your_bucket \
  recipehub-backend
```

### Manual Deployment

```bash
# Build the application
./mvnw clean package -DskipTests

# Run in production
java -jar target/back-end-recipe-hub-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

For detailed deployment instructions, see:

📖 **[Deployment Setup Guide](DEPLOYMENT_SETUP.md)**

## 🌐 URLs

- **Development**: `http://localhost:8080`
- **Production**: `https://back-end-recipe-hub.onrender.com`


## 🔧 Key Features Explained

### Smart Search System
- **Author Search**: Search by `authorId` returns both recipes and recipe books in `AuthorSearchResponse` format
- **Filtered Search**: Combine author with other filters (title, tags, cuisine, difficulty) for precise results
- **Case-Insensitive**: All text searches are case-insensitive across title, author, and tags
- **Public-Only**: Search results only include public content (enforced server-side)
- **Multi-Criteria**: Support for 15+ different search parameters

### Recipe Management
- **Forking System**: Create variations of existing recipes with `originalRecipeId` tracking
- **Soft Delete**: Safe deletion with data preservation for both users and recipes
- **Image Support**: AWS S3 integration for recipe image upload and storage
- **Tagging System**: Categorize recipes with multiple tags (Easy, Quick, Italian, Dessert, etc.)
- **Like System**: Track recipe popularity with like counts
- **Cooked/Favorite**: Mark recipes as cooked or favorite

### User Management
- **Soft Delete**: Users are anonymized rather than permanently deleted
- **Authentication**: Secure registration and login with password encryption
- **Profile Management**: Update user information
- **Recipe Ownership**: Users can manage their own recipes and recipe books

### Recipe Books
- **Collection Management**: Organize recipes into named collections
- **Public/Private**: Control visibility of recipe books
- **Recipe Association**: Link multiple recipes to a single recipe book

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Check the [API Documentation](API_DOCUMENTATION.md)
- Review the [Deployment Setup Guide](DEPLOYMENT_SETUP.md)
- Open an issue on GitHub

## 🔄 Version History

- **v0.0.1-SNAPSHOT**: Production-ready release with comprehensive features
  - Complete user authentication and management system
  - Advanced recipe CRUD operations with forking
  - Smart search system with multi-criteria filtering
  - Recipe book organization and management
  - AWS S3 integration for image storage
  - Comprehensive testing suite (62 tests)
  - Render.com deployment with Docker
  - Production-ready error handling and validation

---

**RecipeHub Backend** - Making recipe sharing and discovery simple and powerful! 🍳✨

**Live Demo**: https://back-end-recipe-hub.onrender.com 