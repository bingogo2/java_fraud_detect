# Real-time Fraud Detection System

This project demonstrates a **proof of concept** for a real-time fraud detection service built using **Java (Spring Boot)** within a **Kubernetes (AWS EKS)** environment. It is designed to process financial transactions asynchronously, detect potential fraud using rule-based logic, and leverage cloud-native services like **AWS SQS**, **CloudWatch**, and **Redis** for messaging, logging, and caching.

## 🎯 Objective

The primary objective of this system is to:
- Detect fraudulent transactions in **real time**
- Be deployed on a **Kubernetes cluster (AWS EKS)**
- Demonstrate **high availability, scalability, and resilience**
- Leverage **cloud-native AWS services** for asynchronous processing, logging, and monitoring

## 🔍 Key Features

| Feature                              | Description                                                                                                                    |
| ------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------ |
| **Real-Time Transaction Processing** | Analyzes incoming financial transactions as they arrive.                                                                       |
| **Rule-Based Fraud Detection**       | Implements configurable rules such as transaction amount thresholds and account history checks.                                |
| **Fraud Notification & Logging**     | Logs detected fraud events using **AWS CloudWatch** for alerting and analysis.                                                 |
| **High Availability**                | Deployed on **AWS EKS** using Kubernetes Deployments and Services.                                                             |
| **Auto-Scaling**                     | Utilizes **Horizontal Pod Autoscaler (HPA)** for dynamic scaling under load.                                                   |
| **Asynchronous Messaging**           | Uses **AWS SQS** to decouple ingestion from processing.                                                                        |
| **Data Caching**                     | Speeds up transaction analysis using **Redis** for fast data retrieval.                                                        |
| **Distributed Logging**              | Centralized logs using **AWS CloudWatch**.                                                                                     |
| **Comprehensive Testing**            | Includes unit tests, integration tests, simulation tests, and performance testing using **JUnit**, **JaCoCo**, and **JMeter**. |

## 🧠 Architecture Overview

Below is a detailed architecture diagram showing how components interact:

```mermaid
graph TD
    A[Incoming Transactions] --> B[(AWS SQS Queue)]
    B --> C[Fraud Detection Service<br/>(Spring Boot App)]
    C --> D[AWS CloudWatch Logs & Alerts]
    C --> E[Redis (Data Caching)]
    C --> F[Kubernetes (AWS EKS Cluster)]
    F --> G[EC2 Instances<br/>(Worker Nodes)]

    style C fill:#4CAF50,stroke:#333,color:white
    style D fill:#2196F3,stroke:#333,color:white
    style E fill:#FFC107,stroke:#333,color:black
    style B fill:#FF9800,stroke:#333,color:black
    style F fill:#9C27B0,stroke:#333,color:white
    style G fill:#795548,stroke:#333,color:white

💡 System Architecture
        The system is designed with the following components:

        Fraud Detection API: The API is responsible for receiving transaction data and processing it to detect fraudulent transactions.

        Redis: Used to store and cache transaction data for quick access.

        AWS SQS: Simple Queue Service is used to manage queues for processing incoming transaction data asynchronously, improving performance.

        AWS CloudWatch: Logs fraud detection results to CloudWatch for monitoring and alerting.

The architecture enables high availability, scalability, and efficient processing of large amounts of transactional data.
                                +--------------------+
                                |    Fraud Detect    |
                                |      API Service   |
                                +--------------------+
                                         |
                                         v
                        +------------------------------+
                        |    Redis (Data Caching)      |
                        +------------------------------+
                                         |
                                         v
                      +----------------------------------+
                      |    AWS SQS (Message Queuing)   |
                      +----------------------------------+
                                         |
                                         v
                     +-----------------------------------+
                     |  AWS CloudWatch (Logging & Alerts)|
                     +-----------------------------------+
                                         |
                                         v
                                +--------------------+
                                |    Kubernetes (EKS)|
                                |    (Containerized) |
                                +--------------------+
                                         |
                                         v
                        +------------------------------+
                        |       EC2 Instances          |
                        |     (AWS Infrastructure)     |
                        +------------------------------+


        | Component             | Description                                                                                   |
        |-----------------------|-----------------------------------------------------------------------------------------------|
        | **Fraud Detection Service** | Core Spring Boot microservice that processes messages from SQS, applies fraud detection logic, uses Redis for fast lookups, and logs results to CloudWatch. |
        | **AWS SQS**           | Asynchronous message queue that decouples transaction ingestion from processing.              |
        | **Redis**             | In-memory cache used to store and retrieve user transaction history and other relevant data quickly. |
        | **AWS CloudWatch**    | Centralized logging and alerting mechanism for fraud events.                                  |
        | **Kubernetes (AWS EKS)** | Container orchestration platform for deploying, scaling, and managing the application.        |
        | **EC2 Instances**     | Underlying compute resources running EKS worker nodes and optionally Redis (if not using ElastiCache). |


🛠️ Technologies Used
        | Category        | Technology                        |
        |-----------------|-----------------------------------|
        | **Language**    | Java 21                           |
        | **Framework**   | Spring Boot 3.4.5                 |
        | **Build Tool**  | Maven                             |
        | **Containerization** | Docker                        |
        | **Orchestration**    | Kubernetes (AWS EKS)         |
        | **Messaging**   | AWS SQS                           |
        | **Logging**     | AWS CloudWatch                    |
        | **Data Store**  | Redis                             |
        | **Testing**     | JUnit, JaCoCo, JMeter             |
        | **Helpers**     | Lombok                            |
        | **Local Dev**   | LocalStack                        |


📦 Prerequisites
        Before building and deploying the system, ensure the following are set up:

        Java 21 SDK
        Maven
        Docker
        kubectl configured for your Kubernetes cluster
        AWS CLI with proper credentials and region set
        An existing AWS EKS Cluster
        An AWS SQS Queue
        An AWS CloudWatch Log Group
        A Redis instance accessible from EKS (can be EC2 or ElastiCache)
        An AWS ECR Repository for Docker images
        Proper IAM permissions for EKS worker nodes
        Optional for local development: 
        LocalStack (to simulate AWS services locally)
        JMeter (for load testing)
📁 Project Structure
        FraudDetectPoc/
        ├── Kubernetes/              # Deployment manifests (.yaml)
        ├── src/
        │   ├── main/java/           # Application source code
        │   ├── test/java/           # Unit & Integration tests
        │   └── test/resources/
        │       └── jmeter/          # JMeter test plans (.jmx) and reports
        ├── target/                  # Maven output directory
        │   ├── FraudDetectPoc-0.0.1-SNAPSHOT.jar
        │   └── jacoco-report/       # Code coverage reports
        ├── Dockerfile               # Docker build instructions
        └── pom.xml                  # Maven configuration

🚀 Setup and Deployment (on AWS EKS)
        1. Clone the repository
                git clone <your-repository-url>
                cd FraudDetectPoc
        2. Build the Java application
                mvn clean package

        3. Build Docker image
        Replace YOUR_ECR_REPO_URI accordingly.
                docker build -t YOUR_ECR_REPO_URI:latest .

        4. Authenticate Docker with AWS ECR
                aws ecr get-login-password --region YOUR_AWS_REGION | docker login --username AWS --password-stdin YOUR_ECR_REPO_URI

        5. Push image to ECR
                docker push YOUR_ECR_REPO_URI:latest
        6. Configure Kubernetes manifests
            Update Kubernetes/deployment.yaml with:
                Correct image URI
                Environment variables (SQS URL, Redis host/port, CloudWatch group name, etc.)

        7. Apply manifests
              kubectl apply -f Kubernetes/

        8. Verify deployment
              kubectl get pods
              kubectl get svc
              kubectl get hpa

🧪 Development and Testing
        Local Development Setup
            Run LocalStack
                docker run --rm -it -p 4566:4566 -e SERVICES=sqs,cloudwatch localstack/localstack:latest

            Run Redis locally
                docker run --name local-redis -d -p 6379:6379 redis

            Running Locally
                Use the dev profile:
                  mvn spring-boot:run -Dspring-boot.run.profiles=dev

            or 

                  java -jar target/FraudDetectPoc-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

        Running Tests
            Unit & Integration Tests
                mvn test

            Code Coverage Report
                Generated automatically during mvn package. Open:
                    target/jacoco-report/index.html

            Load Testing
                Use JMeter test plans located in src/test/resources/jmeter/.

📦 Deliverables
    This project includes:
        Source code (src/)
        Kubernetes manifests (Kubernetes/)
        Dockerfile
        Test coverage report (target/jacoco-report/)
        JMeter performance test plans and reports
        This documentation

👥 Contributing
        Contributions are welcome! Please fork the repo and submit a pull request.

📄 License
        none