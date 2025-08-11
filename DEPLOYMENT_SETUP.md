# 🚀 Render.com Deployment Setup

This document outlines the deployment setup for RecipeHub Backend on Render.com using Docker containers.

## 📋 Overview

### Render.com Deployment
- **Platform**: Render.com with Docker containers
- **Database**: Managed PostgreSQL service
- **Build**: Multi-stage Docker build
- **Deployment**: Automatic deployment from GitHub

### 1. Render.com Service Setup

#### Web Service Configuration
1. **Connect Repository**: Link GitHub repository to Render
2. **Build Command**: `mvn clean package -DskipTests`
3. **Start Command**: `java -jar target/back-end-recipe-hub-0.0.1-SNAPSHOT.jar`
4. **Environment**: Docker

#### Environment Variables
Set these in Render Dashboard → Environment Variables:

**Database Configuration:**
```
JDBC_DATABASE_URL=jdbc:postgresql://host:port/database
JDBC_DATABASE_USERNAME=your_username
JDBC_DATABASE_PASSWORD=your_password
```

**AWS S3 Configuration:**
```
AWS_S3_BUCKET_NAME=your-s3-bucket
AWS_REGION=us-east-2
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

**Application Configuration:**
```
SPRING_PROFILES_ACTIVE=render
PORT=8080
```

### 2. Database Setup

#### PostgreSQL Database
1. **Create Database**: Use Render's managed PostgreSQL service
2. **Connection**: Render automatically provides connection details
3. **Migration**: Application auto-creates tables on startup

### 3. Deployment Process

#### Automatic Deployment
1. **Push to main** triggers automatic deployment
2. **Build Process**: Docker multi-stage build
3. **Health Checks**: Built-in health monitoring
4. **Auto-scaling**: Automatic scaling based on traffic

#### Manual Deployment
```bash
# Build Docker image locally
docker build -t recipehub-backend .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=render \
  -e JDBC_DATABASE_URL=your_db_url \
  -e AWS_S3_BUCKET_NAME=your_bucket \
  recipehub-backend
```

### 4. Configuration Files

**Essential Files:**
- **`Dockerfile`** - Multi-stage Docker build configuration
- **`application-render.properties`** - Render-specific configuration
- **`pom.xml`** - Maven build configuration

### 5. Troubleshooting

#### Common Issues
1. **Build Failures**: Check Maven build logs in Render dashboard
2. **Database Connection**: Verify JDBC_DATABASE_URL format
3. **Environment Variables**: Ensure all required variables are set
4. **Health Checks**: Monitor `/actuator/health` endpoint

#### Health Monitoring
- **Endpoint**: `https://back-end-recipe-hub.onrender.com/actuator/health`
- **Logs**: Available in Render dashboard
- **Metrics**: Built-in monitoring and alerting

### 6. Production URL

**Live Application**: https://back-end-recipe-hub.onrender.com

### 7. Benefits of Render.com Deployment

- **Automatic Scaling**: Scales based on traffic
- **Managed Database**: PostgreSQL service included
- **SSL/TLS**: Automatic HTTPS certificates
- **Global CDN**: Fast global access
- **Zero Downtime**: Rolling deployments
- **Cost Effective**: Pay-per-use pricing model

---

**Note**: This deployment setup replaces the previous AWS Elastic Beanstalk configuration. The application is now fully containerized and deployed on Render.com for better scalability and ease of management. 