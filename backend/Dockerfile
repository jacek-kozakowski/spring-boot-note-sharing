FROM openjdk:21-jdk-slim

# Install docker client and netcat
RUN apt-get update && apt-get install -y docker.io netcat-openbsd && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src
COPY start.sh .

RUN ./mvnw clean package -DskipTests
RUN chmod +x start.sh

EXPOSE 8080

CMD ["./start.sh"]

