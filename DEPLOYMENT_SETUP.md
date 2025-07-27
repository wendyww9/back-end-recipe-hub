# 🚀 AWS Elastic Beanstalk Deployment Setup

This guide will help you set up automatic deployment to AWS Elastic Beanstalk using GitHub Actions.

## 📋 Prerequisites

1. **AWS Account** with Elastic Beanstalk access
2. **GitHub Repository** with your Spring Boot application
3. **S3 Bucket** for storing deployment packages (if using AWS CLI method)

## 🔧 Setup Steps

### 1. Create Elastic Beanstalk Application & Environment

```bash
# Install EB CLI locally (optional, for testing)
pip install awsebcli

# Initialize EB application
eb init recipehub-backend --platform "Java 17" --region us-east-1

# Create environment
eb create recipehub-backend-prod --instance-type t2.micro --single-instance
```

### 2. Configure GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions, and add:

#### Required Secrets:
- `AWS_ACCESS_KEY_ID` - Your AWS access key
- `AWS_SECRET_ACCESS_KEY` - Your AWS secret key
- `S3_BUCKET` - S3 bucket name for deployments (only for AWS CLI method)

#### Optional Secrets:
- `AWS_REGION` - AWS region (default: us-east-1)
- `EB_ENVIRONMENT_NAME` - EB environment name (default: recipehub-backend)
- `EB_APPLICATION_NAME` - EB application name (default: recipehub-backend)

### 3. Update Environment Variables

Edit the workflow files and update these variables:

```yaml
env:
  AWS_REGION: us-east-1  # Your AWS region
  EB_ENVIRONMENT_NAME: your-environment-name
  EB_APPLICATION_NAME: your-application-name
```

### 4. Choose Your Workflow

Two workflow options are provided:

#### Option A: EB CLI Workflow (`.github/workflows/deploy-to-elastic-beanstalk.yml`)
- ✅ Simpler setup
- ✅ Uses EB CLI directly
- ❌ May have dependency issues

#### Option B: AWS CLI Workflow (`.github/workflows/deploy-to-eb-aws-cli.yml`)
- ✅ More reliable
- ✅ Better error handling
- ✅ Requires S3 bucket setup

### 5. Enable Health Check Endpoint

Add Spring Boot Actuator to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 🔐 AWS IAM Permissions

Your AWS user needs these permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "elasticbeanstalk:*",
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": "*"
        }
    ]
}
```

## 🚀 Deployment

Once configured, deployments will automatically trigger when you:

1. **Push to main branch** - Automatic deployment
2. **Manual trigger** - Go to Actions tab → Select workflow → Run workflow

## 📊 Monitoring

- **GitHub Actions**: Check the Actions tab for deployment status
- **AWS Console**: Monitor your EB environment
- **Application Logs**: View logs in EB console or via `eb logs`

## 🔧 Troubleshooting

### Common Issues:

1. **Permission Denied**: Check AWS credentials and IAM permissions
2. **Build Failures**: Ensure Maven build works locally
3. **Health Check Fails**: Verify actuator endpoint is accessible
4. **Environment Not Found**: Check EB environment name in workflow

### Debug Commands:

```bash
# Check EB environment status
eb status

# View application logs
eb logs

# SSH into instance (if enabled)
eb ssh
```

## 📝 Notes

- The workflow skips tests during deployment (`-DskipTests`)
- Health check waits 60-90 seconds for application startup
- Deployment packages are cleaned to exclude unnecessary files
- Both workflows include proper error handling and status reporting 