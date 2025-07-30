# RecipeHub Backend

Spring Boot REST API for recipe management with user authentication and recipe interactions.

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL (for production)

## Quick Start

### Development
```bash
# Clone and setup
git clone https://github.com/wendyww9/back-end-recipe-hub.git
cd back-end-recipe-hub

# Run tests
./mvnw test

# Start development server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production
# Use Maven
```bash
# Run with production profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

```bash
# Build
./mvnw clean package -DskipTests

# Run with production profile
java -jar target/back-end-recipe-hub-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Environment Variables

### Production
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/recipehub
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password
```

## URLs

- **Development**: `http://localhost:8080`
- **Production**: `http://recipehub-dev-env.eba-6mi9w35s.us-east-2.elasticbeanstalk.com`

## Key Endpoints

- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login
- `GET /api/recipes` - Get all recipes
- `POST /api/recipes` - Create recipe
- `GET /api/recipes/search?title={title}` - Search recipes

## API Documentation

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete endpoint details.

## Deployment

### AWS Elastic Beanstalk
- Automatic deployment on push to main branch
- Configure environment variables in EB console

### Manual
```bash
./mvnw clean package -DskipTests
java -jar target/back-end-recipe-hub-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
``` 