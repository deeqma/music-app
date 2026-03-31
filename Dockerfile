FROM maven:3-amazoncorretto-25 AS build
WORKDIR /app/myapp
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM amazoncorretto:25-alpine
WORKDIR /app/myapp
COPY --from=build /app/myapp/target/ROOT.jar app.jar
COPY src/main/resources/localstorage/mp3 /app/default-songs
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh
ENTRYPOINT ["sh", "entrypoint.sh"]