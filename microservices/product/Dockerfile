
FROM eclipse-temurin:17-jdk as extract
WORKDIR /workspace
COPY build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jdk as runtime
WORKDIR /app
COPY --from=extract /workspace/dependencies/ ./dependencies/
COPY --from=extract /workspace/snapshot-dependencies/ ./snapshot-dependencies/
COPY --from=extract /workspace/resources/ ./resources/
COPY --from=extract /workspace/application/ ./application/
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-cp", "resources:application:dependencies/*:snapshot-dependencies/*", "msa.lime1st.product.ProductApplication"]
