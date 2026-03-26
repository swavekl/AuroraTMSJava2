# ======================================
# AuroraTMS Application Image
# ======================================
FROM aurora/tesseract-al2023:5.3

# Set working directory inside container
WORKDIR /usr/local/auroratms

# Copy application artifacts
COPY server-2.0.0.jar auroratms.jar
COPY vault.properties .
COPY ca.pem .

# Install Java (Amazon Corretto 17) only to use keytool
RUN dnf install -y java-17-amazon-corretto-devel \
    && dnf clean all

# Import SSL certificate into Java truststore
RUN keytool -import -trustcacerts \
    -alias auroratms-ca \
    -file /usr/local/auroratms/ca.pem \
    -cacerts \
    -storepass changeit \
    -noprompt

# Copy prebuilt tessdata from base or build stage
COPY --from=ocr-base /build/tesseract-5.3.3/tessdata /usr/share/tesseract/tessdata

# Environment variables
ENV TESSDATA_PREFIX=/usr/share/tesseract/tessdata

# Environment variables for Tesseract
ENV TESSDATA_PREFIX=/usr/share/tesseract/tessdata
ENV LD_LIBRARY_PATH=/usr/lib64:/usr/local/lib

# Run the application
ENTRYPOINT ["java", \
            "-Djna.library.path=/usr/lib64", \
            "-Dtessdata.prefix=/usr/share/tesseract/tessdata/", \
            "--add-modules", "java.se", \
            "--add-exports", "java.base/jdk.internal.ref=ALL-UNNAMED", \
            "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
            "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", \
            "--add-opens", "java.base/java.net=ALL-UNNAMED", \
            "--add-opens", "java.management/sun.management=ALL-UNNAMED", \
            "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED", \
            "-jar", "auroratms.jar"]