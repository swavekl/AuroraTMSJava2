# ======================================
# AuroraTMS Application Image
# ======================================
FROM aurora/tesseract-al2023:5.3

# Set working directory inside container
WORKDIR /usr/local/auroratms

# Copy application artifacts - BULLETPROOF DIRECT DESTINATIONS
COPY server-2.0.0.jar /usr/local/auroratms/auroratms.jar
COPY vault.properties /usr/local/auroratms/vault.properties
COPY ca.pem /usr/local/auroratms/ca.pem

# Install Java (Amazon Corretto 17)
# The base image is Amazon Linux 2023, so dnf works perfectly here.
RUN dnf install -y java-17-amazon-corretto-devel \
    && dnf clean all

# Import SSL certificate into Java truststore
RUN keytool -import -trustcacerts \
    -alias auroratms-ca \
    -file /usr/local/auroratms/ca.pem \
    -cacerts \
    -storepass changeit \
    -noprompt

# Copy the traineddata files
COPY --from=aurora/tesseract-al2023:5.3 /usr/share/tesseract/tessdata/*.traineddata /usr/share/tesseract/tessdata/

# Create a symbolic link in the application directory
RUN ln -s /usr/share/tesseract/tessdata /usr/local/auroratms/tessdata

# Environment variables for Tesseract and Linker
ENV TESSDATA_PREFIX=/usr/share/tesseract/tessdata/
ENV LD_LIBRARY_PATH=/usr/local/lib:/usr/lib64:$LD_LIBRARY_PATH

# Run the application
ENTRYPOINT ["java", \
            "-Djna.library.path=/usr/local/lib:/usr/lib64", \
            "-Dtessdata.prefix=/usr/share/tesseract/tessdata/", \
            "--add-modules", "java.se", \
            "--add-exports", "java.base/jdk.internal.ref=ALL-UNNAMED", \
            "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
            "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", \
            "--add-opens", "java.base/java.net=ALL-UNNAMED", \
            "--add-opens", "java.management/java.lang.management=ALL-UNNAMED", \
            "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED", \
            "-jar", "auroratms.jar"]
