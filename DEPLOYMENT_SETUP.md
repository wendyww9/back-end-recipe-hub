# 🚀 AWS Elastic Beanstalk Deployment Setup

This guide documents the current working deployment setup for the RecipeHub backend application.

## 📋 Current Configuration

### GitHub Actions Workflow
- **File**: `.github/workflows/deploy-to-eb.yml`
- **Trigger**: Push to `main` branch
- **Platform**: Java 17 on Amazon Linux 2023

### Deployment Process
1. **Build**: Maven builds the JAR file
2. **Copy**: JAR is copied to root directory
3. **Deploy**: EB CLI deploys to Elastic Beanstalk
4. **Health Check**: Verifies deployment with `/actuator/health`

## 🔧 Required Setup

### 1. AWS Elastic Beanstalk Environment
- **Application**: `recipehub-dev`
- **Environment**: `Recipehub-dev-env`
- **Platform**: Corretto 17 running on 64bit Amazon Linux 2023
- **Region**: `us-east-2`

### 2. GitHub Secrets
Configure these in GitHub repository → Settings → Secrets and variables → Actions:

```
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=us-east-2
EB_ENVIRONMENT_NAME=Recipehub-dev-env
```

### 3. Environment Variables in EB Console
Set these in AWS EB Console → Configuration → Software → Environment Properties:

```
DATABASE_URL=jdbc:postgresql://your-rds-endpoint:5432/your-database
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password
```

## 📁 Key Files

### Deployment Configuration
- **`.github/workflows/deploy-to-eb.yml`** - GitHub Actions workflow
- **`Procfile`** - EB startup command
- **`.elasticbeanstalk/config.yml`** - EB configuration
- **`.ebignore`** - Files to exclude from deployment

### Application Configuration
- **`src/main/resources/application.properties`** - Environment variables for database
- **`pom.xml`** - Spring Boot Actuator dependency

## 🚀 How It Works

1. **Push to main** triggers GitHub Actions
2. **Maven builds** the application JAR
3. **JAR is copied** to root directory
4. **EB CLI deploys** the package
5. **Health check** verifies `/actuator/health` endpoint
6. **Application starts** with database connection

## 🔍 Health Monitoring

- **EB Environment Health**: Monitored in AWS Console
- **Application Health**: `/actuator/health` endpoint
- **Database Health**: Included in actuator health check

## 🛠️ Troubleshooting

### Common Issues
1. **JAR not found**: Ensure JAR copy step in workflow
2. **Database connection**: Check environment variables in EB console
3. **Health check fails**: Verify actuator endpoint is accessible

### Debug Commands
```bash
# Check deployment status
eb status

# View application logs
eb logs

# Test health endpoint
curl http://your-eb-url/actuator/health
```

## 📝 Notes

- **Security**: Database credentials are in EB environment variables, not in code
- **Build**: Maven build happens in GitHub Actions, not on EB instance
- **Health Check**: Uses Spring Boot Actuator for reliable health monitoring
- **Clean History**: All deployment setup is in one clean commit 