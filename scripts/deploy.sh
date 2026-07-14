#!/bin/bash
set -euo pipefail

ACCOUNT_ID="442797924420"
REGION="ap-northeast-1"
ECR_REPO="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/cdk-hnb659fds-container-assets-${ACCOUNT_ID}-${REGION}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Step 1: Build Docker images ==="

echo "Building backend..."
docker build --network=sagemaker -t attendance-backend "${PROJECT_DIR}/backend"

echo "Building frontend..."
docker build --network=sagemaker -t attendance-frontend "${PROJECT_DIR}/frontend"

echo "=== Step 2: Push images to ECR ==="

# Assume image-publishing role for ECR access
CREDS=$(aws sts assume-role \
  --role-arn "arn:aws:iam::${ACCOUNT_ID}:role/cdk-hnb659fds-image-publishing-role-${ACCOUNT_ID}-${REGION}" \
  --role-session-name ImagePublish --output json)

export AWS_ACCESS_KEY_ID=$(echo $CREDS | jq -r '.Credentials.AccessKeyId')
export AWS_SECRET_ACCESS_KEY=$(echo $CREDS | jq -r '.Credentials.SecretAccessKey')
export AWS_SESSION_TOKEN=$(echo $CREDS | jq -r '.Credentials.SessionToken')

aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com

BACKEND_TAG="${ECR_REPO}:backend-$(date +%Y%m%d%H%M%S)"
FRONTEND_TAG="${ECR_REPO}:frontend-$(date +%Y%m%d%H%M%S)"

docker tag attendance-backend "${BACKEND_TAG}"
docker tag attendance-frontend "${FRONTEND_TAG}"
docker push "${BACKEND_TAG}"
docker push "${FRONTEND_TAG}"

echo "Backend image: ${BACKEND_TAG}"
echo "Frontend image: ${FRONTEND_TAG}"

# Clear image-publishing creds
unset AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN

echo "=== Step 3: Deploy CloudFormation ==="

# Assume deploy role for CloudFormation access
CREDS=$(aws sts assume-role \
  --role-arn "arn:aws:iam::${ACCOUNT_ID}:role/cdk-hnb659fds-deploy-role-${ACCOUNT_ID}-${REGION}" \
  --role-session-name CFDeploy --output json)

export AWS_ACCESS_KEY_ID=$(echo $CREDS | jq -r '.Credentials.AccessKeyId')
export AWS_SECRET_ACCESS_KEY=$(echo $CREDS | jq -r '.Credentials.SecretAccessKey')
export AWS_SESSION_TOKEN=$(echo $CREDS | jq -r '.Credentials.SessionToken')

STACK_NAME="AttendanceStack"
TEMPLATE_FILE="${PROJECT_DIR}/infra/cfn-template.yaml"

# Generate CloudFormation template from CDK (using synth output)
cd "${PROJECT_DIR}/infra"

# Create a simplified CFn template with the actual image URIs
cat > "${TEMPLATE_FILE}" << CFNEOF
AWSTemplateFormatVersion: '2010-09-09'
Description: Attendance App - ECS Fargate

Parameters:
  BackendImage:
    Type: String
    Default: ${BACKEND_TAG}
  FrontendImage:
    Type: String
    Default: ${FRONTEND_TAG}

Resources:
  Vpc:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: 10.0.0.0/16
      EnableDnsHostnames: true
      EnableDnsSupport: true
      Tags:
        - Key: Name
          Value: attendance-vpc

  InternetGateway:
    Type: AWS::EC2::InternetGateway

  VpcGatewayAttachment:
    Type: AWS::EC2::VPCGatewayAttachment
    Properties:
      VpcId: !Ref Vpc
      InternetGatewayId: !Ref InternetGateway

  PublicSubnet1:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !Ref Vpc
      CidrBlock: 10.0.1.0/24
      AvailabilityZone: !Select [0, !GetAZs '']
      MapPublicIpOnLaunch: true

  PublicSubnet2:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !Ref Vpc
      CidrBlock: 10.0.2.0/24
      AvailabilityZone: !Select [1, !GetAZs '']
      MapPublicIpOnLaunch: true

  PublicRouteTable:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref Vpc

  PublicRoute:
    Type: AWS::EC2::Route
    DependsOn: VpcGatewayAttachment
    Properties:
      RouteTableId: !Ref PublicRouteTable
      DestinationCidrBlock: 0.0.0.0/0
      GatewayId: !Ref InternetGateway

  PublicSubnet1RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PublicSubnet1
      RouteTableId: !Ref PublicRouteTable

  PublicSubnet2RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PublicSubnet2
      RouteTableId: !Ref PublicRouteTable

  EcsCluster:
    Type: AWS::ECS::Cluster
    Properties:
      ClusterName: attendance-cluster

  TaskExecutionRole:
    Type: AWS::IAM::Role
    Properties:
      AssumeRolePolicyDocument:
        Version: '2012-10-17'
        Statement:
          - Effect: Allow
            Principal:
              Service: ecs-tasks.amazonaws.com
            Action: sts:AssumeRole
      ManagedPolicyArns:
        - arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

  TaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: attendance-task
      Cpu: '512'
      Memory: '1024'
      NetworkMode: awsvpc
      RequiresCompatibilities: [FARGATE]
      ExecutionRoleArn: !GetAtt TaskExecutionRole.Arn
      ContainerDefinitions:
        - Name: backend
          Image: !Ref BackendImage
          Essential: true
          PortMappings:
            - ContainerPort: 8080
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: !Ref BackendLogGroup
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: backend
        - Name: frontend
          Image: !Ref FrontendImage
          Essential: true
          PortMappings:
            - ContainerPort: 3000
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-group: !Ref FrontendLogGroup
              awslogs-region: !Ref AWS::Region
              awslogs-stream-prefix: frontend

  BackendLogGroup:
    Type: AWS::Logs::LogGroup
    Properties:
      LogGroupName: /ecs/attendance/backend
      RetentionInDays: 7

  FrontendLogGroup:
    Type: AWS::Logs::LogGroup
    Properties:
      LogGroupName: /ecs/attendance/frontend
      RetentionInDays: 7

  AlbSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: ALB Security Group
      VpcId: !Ref Vpc
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 80
          ToPort: 80
          CidrIp: 0.0.0.0/0

  TaskSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: ECS Task Security Group
      VpcId: !Ref Vpc
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 3000
          ToPort: 3000
          SourceSecurityGroupId: !Ref AlbSecurityGroup

  LoadBalancer:
    Type: AWS::ElasticLoadBalancingV2::LoadBalancer
    Properties:
      Scheme: internet-facing
      Subnets:
        - !Ref PublicSubnet1
        - !Ref PublicSubnet2
      SecurityGroups:
        - !Ref AlbSecurityGroup

  TargetGroup:
    Type: AWS::ElasticLoadBalancingV2::TargetGroup
    Properties:
      Port: 3000
      Protocol: HTTP
      VpcId: !Ref Vpc
      TargetType: ip
      HealthCheckPath: /
      HealthCheckIntervalSeconds: 30
      HealthyThresholdCount: 2
      UnhealthyThresholdCount: 5

  Listener:
    Type: AWS::ElasticLoadBalancingV2::Listener
    Properties:
      LoadBalancerArn: !Ref LoadBalancer
      Port: 80
      Protocol: HTTP
      DefaultActions:
        - Type: forward
          TargetGroupArn: !Ref TargetGroup

  EcsService:
    Type: AWS::ECS::Service
    DependsOn: Listener
    Properties:
      Cluster: !Ref EcsCluster
      TaskDefinition: !Ref TaskDefinition
      DesiredCount: 1
      LaunchType: FARGATE
      NetworkConfiguration:
        AwsvpcConfiguration:
          AssignPublicIp: ENABLED
          Subnets:
            - !Ref PublicSubnet1
            - !Ref PublicSubnet2
          SecurityGroups:
            - !Ref TaskSecurityGroup
      LoadBalancers:
        - ContainerName: frontend
          ContainerPort: 3000
          TargetGroupArn: !Ref TargetGroup

Outputs:
  LoadBalancerDNS:
    Description: ALB DNS Name
    Value: !GetAtt LoadBalancer.DNSName
CFNEOF

echo "Deploying CloudFormation stack..."
aws cloudformation deploy \
  --stack-name "${STACK_NAME}" \
  --template-file "${TEMPLATE_FILE}" \
  --parameter-overrides \
    BackendImage="${BACKEND_TAG}" \
    FrontendImage="${FRONTEND_TAG}" \
  --capabilities CAPABILITY_IAM \
  --region "${REGION}" \
  --no-fail-on-empty-changeset

echo "=== Deployment complete ==="
aws cloudformation describe-stacks --stack-name "${STACK_NAME}" \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerDNS`].OutputValue' \
  --output text
