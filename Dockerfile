FROM openjdk:21-jdk-slim
COPY bank/build/libs/bankKotlinFat.jar /app/bankKotlin.jar
ENTRYPOINT ["java", "-jar", "/app/bankKotlin.jar"]