import * as cdk from "aws-cdk-lib";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ecs_patterns from "aws-cdk-lib/aws-ecs-patterns";
import { NetworkMode } from "aws-cdk-lib/aws-ecr-assets";
import * as path from "path";
import { Construct } from "constructs";

export class AttendanceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = new ec2.Vpc(this, "Vpc", {
      maxAzs: 2,
      natGateways: 1,
    });

    const cluster = new ecs.Cluster(this, "Cluster", { vpc });

    const taskDefinition = new ecs.FargateTaskDefinition(this, "TaskDef", {
      memoryLimitMiB: 1024,
      cpu: 512,
    });

    taskDefinition.addContainer("backend", {
      image: ecs.ContainerImage.fromAsset(
        path.join(__dirname, "../../backend"),
        { networkMode: NetworkMode.custom("sagemaker") }
      ),
      logging: ecs.LogDrivers.awsLogs({ streamPrefix: "backend" }),
      environment: {
        SPRING_PROFILES_ACTIVE: "default",
      },
      portMappings: [{ containerPort: 8080 }],
    });

    taskDefinition.addContainer("frontend", {
      image: ecs.ContainerImage.fromAsset(
        path.join(__dirname, "../../frontend"),
        { networkMode: NetworkMode.custom("sagemaker") }
      ),
      logging: ecs.LogDrivers.awsLogs({ streamPrefix: "frontend" }),
      portMappings: [{ containerPort: 3000 }],
    });

    const service =
      new ecs_patterns.ApplicationLoadBalancedFargateService(
        this,
        "Service",
        {
          cluster,
          taskDefinition,
          desiredCount: 1,
          publicLoadBalancer: true,
          assignPublicIp: true,
        }
      );

    service.targetGroup.configureHealthCheck({
      path: "/api/employees",
      healthyHttpCodes: "200",
      interval: cdk.Duration.seconds(30),
    });

    new cdk.CfnOutput(this, "LoadBalancerDNS", {
      value: service.loadBalancer.loadBalancerDnsName,
      description: "ALB DNS name",
    });
  }
}
