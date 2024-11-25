FROM openjdk:17
ADD build/libs/wallet-SNAPSHOT.jar wallet-SNAPSHOT.jar
EXPOSE 8081 6379
ENTRYPOINT ["java", "-jar", "wallet-SNAPSHOT.jar"]