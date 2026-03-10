# ============================================================
# Keycloak SPI Docker Image
# Bundles the Tencent SMS SPI JAR into quay.io/keycloak/keycloak:26.5.5
# Three-stage build:
#   1. kc-source    – extracts Keycloak JARs for compilation
#   2. jar-builder  – Maven + JDK 17 compiles the SPI JAR
#   3. kc-builder   – Keycloak build step indexes the new provider
#   4. runtime      – minimal final image
# ============================================================

# ── Stage 0: Keycloak source image (JARs for compile-time deps) ──────────────
FROM quay.io/keycloak/keycloak:26.5.5 AS kc-source

# ── Stage 1: compile SPI JAR ─────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS jar-builder
WORKDIR /build

# Copy the 4 required Keycloak JARs by their exact filenames (confirmed from the image)
COPY --from=kc-source /opt/keycloak/lib/lib/main/org.keycloak.keycloak-core-26.5.5.jar /kc-jars/keycloak-core-26.5.5.jar
COPY --from=kc-source /opt/keycloak/lib/lib/main/org.keycloak.keycloak-server-spi-26.5.5.jar /kc-jars/keycloak-server-spi-26.5.5.jar
COPY --from=kc-source /opt/keycloak/lib/lib/main/org.keycloak.keycloak-server-spi-private-26.5.5.jar /kc-jars/keycloak-server-spi-private-26.5.5.jar
COPY --from=kc-source /opt/keycloak/lib/lib/main/org.keycloak.keycloak-services-26.5.5.jar /kc-jars/keycloak-services-26.5.5.jar

# Install them into the local Maven repo so pom.xml can use provided scope reliably
RUN mvn install:install-file -q -Dpackaging=jar -DgroupId=org.keycloak \
      -DartifactId=keycloak-core            -Dversion=26.5.5 -Dfile=/kc-jars/keycloak-core-26.5.5.jar && \
    mvn install:install-file -q -Dpackaging=jar -DgroupId=org.keycloak \
      -DartifactId=keycloak-server-spi      -Dversion=26.5.5 -Dfile=/kc-jars/keycloak-server-spi-26.5.5.jar && \
    mvn install:install-file -q -Dpackaging=jar -DgroupId=org.keycloak \
      -DartifactId=keycloak-server-spi-private -Dversion=26.5.5 -Dfile=/kc-jars/keycloak-server-spi-private-26.5.5.jar && \
    mvn install:install-file -q -Dpackaging=jar -DgroupId=org.keycloak \
      -DartifactId=keycloak-services        -Dversion=26.5.5 -Dfile=/kc-jars/keycloak-services-26.5.5.jar

COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# ── Stage 2: register provider with Keycloak ─────────────────
FROM quay.io/keycloak/keycloak:26.5.5 AS kc-builder
COPY --from=jar-builder /build/target/keycloak-tencent-sms-spi-*.jar /opt/keycloak/providers/
COPY src/main/resources/theme/sms-otp /opt/keycloak/themes/sms-otp
RUN /opt/keycloak/bin/kc.sh build

# ── Stage 3: runtime image ───────────────────────────────────
FROM quay.io/keycloak/keycloak:26.5.5
COPY --from=kc-builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
