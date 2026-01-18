# Separater E-Mail-Service mit Kafka – Produktionsreife Architektur

## 1. Architektur-Überblick

### 1.1 Komponenten

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Microservice-Architektur                          │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐         ┌──────────────────┐         ┌───────────────┐
│  Patient-Customer│         │   Kafka Broker   │         │ Email-Service │
│     Service      │         │                  │         │               │
├──────────────────┤         ├──────────────────┤         ├───────────────┤
│                  │         │                  │         │               │
│ BookingService   │ ──────► │ Topic:           │ ──────► │ Kafka         │
│       │          │ publish │ email-events     │ consume │ Consumer      │
│       ▼          │         │                  │         │   │           │
│ KafkaProducer    │         │ Partitions: 3    │         │   ▼           │
│                  │         │ Replication: 2   │         │ EmailService  │
└──────────────────┘         │                  │         │   │           │
                             └──────────────────┘         │   ▼           │
                                                          │ JavaMailSender│
                                                          │   │           │
                                                          └───┼───────────┘
                                                              │
                                                              ▼
                                                       Gmail SMTP Server
```

### 1.2 Event-Flow

```
1. Patient bucht Termin
2. BookingService speichert Buchung
3. BookingService publiziert Event auf Kafka
4. E-Mail-Service konsumiert Event
5. E-Mail-Service versendet E-Mail
6. E-Mail-Service protokolliert Ergebnis
```

---

## 2. Warum Kafka?

### 2.1 Vorteile

| Feature | Vorteil |
|---------|---------|
| **Asynchronität** | Buchung blockiert nicht auf E-Mail-Versand |
| **Entkopplung** | Services kennen sich nicht direkt |
| **Skalierbarkeit** | E-Mail-Service kann unabhängig skaliert werden |
| **Fehlertoleranz** | Events bleiben erhalten bei Service-Ausfall |
| **Replay** | Events können wiederholt werden |
| **Monitoring** | Kafka bietet Metriken und Monitoring |

### 2.2 Kafka-Konzepte

- **Topic**: Logischer Kanal für Events (z.B. `email-events`)
- **Partition**: Parallelisierung (mehrere Consumer)
- **Consumer Group**: Load Balancing zwischen Instanzen
- **Offset**: Position im Event-Stream (für Replay)
- **Replication**: Datenredundanz

---

## 3. Setup – Lokale Kafka-Installation

### 3.1 Docker Compose Setup

Erstelle `docker-compose.yml` im Projektroot:

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    hostname: zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    hostname: kafka
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181

  postgres-customer:
    image: postgres:15
    container_name: postgres-customer
    environment:
      POSTGRES_DB: customer_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres-customer-data:/var/lib/postgresql/data

  postgres-email:
    image: postgres:15
    container_name: postgres-email
    environment:
      POSTGRES_DB: email_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5434:5432"
    volumes:
      - postgres-email-data:/var/lib/postgresql/data

volumes:
  postgres-customer-data:
  postgres-email-data:
```

### 3.2 Kafka starten

```bash
docker-compose up -d

# Logs prüfen
docker-compose logs -f kafka

# Kafka UI öffnen
open http://localhost:8090
```

---

## 4. Event-Modell

### 4.1 Event-Struktur

```java
package com.medical.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmedEvent {
    
    private String eventId;           // UUID
    private String eventType;         // "BOOKING_CONFIRMED"
    private LocalDateTime timestamp;
    
    // Payload
    private Long bookingId;
    private String recipientEmail;
    private String patientName;
    private String doctorName;
    private LocalDateTime appointmentStartTime;
    private LocalDateTime appointmentEndTime;
    private String practiceName;
    private String practiceAddress;
    private String practicePhone;
}
```

### 4.2 Event-Typen (Erweiterbar)

```java
public enum EmailEventType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    BOOKING_REMINDER,      // 24h vor Termin
    PASSWORD_RESET,
    REGISTRATION_WELCOME
}
```

---

## 5. Patient-Customer Service – Kafka Producer

### 5.1 Dependencies (`pom.xml`)

```xml
<dependencies>
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- JSON Serialization -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
</dependencies>
```

### 5.2 Kafka Configuration

```java
package test.patient_customer.infrastructure.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### 5.3 Application Properties

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all
      retries: 3
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

app:
  kafka:
    topic:
      email-events: email-events
```

### 5.4 Event Publisher Service

```java
package test.patient_customer.infrastructure.messaging;

import com.medical.common.events.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.email-events}")
    private String emailEventsTopic;

    public void publishBookingConfirmedEvent(BookingConfirmedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("BOOKING_CONFIRMED");
        event.setTimestamp(LocalDateTime.now());

        String key = event.getBookingId().toString();

        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(emailEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event published: {} to partition {}", 
                    event.getEventId(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish event: {}", event.getEventId(), ex);
                // Optional: Speichern in Outbox-Tabelle für Retry
            }
        });
    }
}
```

### 5.5 BookingService Integration

```java
package test.patient_customer.application.service;

import com.medical.common.events.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.patient_customer.infrastructure.messaging.EmailEventPublisher;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PatientRepository patientRepository;
    private final ProviderClient providerClient;
    private final EmailEventPublisher emailEventPublisher;

    @Transactional
    public BookingResponse createBooking(Long patientId, Long slotId) {
        log.info("Erstelle Buchung für Patient {} und Slot {}", patientId, slotId);

        // 1. Patient laden
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new EntityNotFoundException("Patient nicht gefunden"));

        // 2. Slot beim Provider-Service buchen
        SlotDto slot = providerClient.bookSlot(slotId);

        // 3. Buchung erstellen
        Booking booking = Booking.builder()
            .patientId(patientId)
            .doctorId(slot.getDoctorId())
            .slotId(slotId)
            .status(BookingStatus.CONFIRMED)
            .createdAt(LocalDateTime.now())
            .build();

        booking = bookingRepository.save(booking);

        // 4. Event publizieren (asynchron!)
        BookingConfirmedEvent event = BookingConfirmedEvent.builder()
            .bookingId(booking.getId())
            .recipientEmail(patient.getEmail())
            .patientName(patient.getFirstName() + " " + patient.getLastName())
            .doctorName(slot.getDoctorName())
            .appointmentStartTime(slot.getStartTime())
            .appointmentEndTime(slot.getEndTime())
            .practiceName(slot.getPracticeName())
            .practiceAddress(slot.getPracticeAddress())
            .practicePhone(slot.getPracticePhone())
            .build();

        emailEventPublisher.publishBookingConfirmedEvent(event);

        log.info("Buchung {} erfolgreich erstellt und Event publiziert", booking.getId());

        return BookingMapper.toResponse(booking);
    }
}
```

---

## 6. Email-Service – Neuer Microservice

### 6.1 Projekt-Struktur

```
email-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/medical/emailservice/
│   │   │       ├── EmailServiceApplication.java
│   │   │       ├── config/
│   │   │       │   ├── KafkaConsumerConfig.java
│   │   │       │   └── MailConfig.java
│   │   │       ├── messaging/
│   │   │       │   └── EmailEventConsumer.java
│   │   │       ├── service/
│   │   │       │   ├── EmailService.java
│   │   │       │   └── EmailServiceImpl.java
│   │   │       ├── domain/
│   │   │       │   ├── EmailLog.java
│   │   │       │   └── EmailStatus.java
│   │   │       └── repository/
│   │   │           └── EmailLogRepository.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── templates/
│   │           └── email/
│   │               └── booking-confirmation.html
│   └── test/
└── README.md
```

### 6.2 Dependencies (`pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.medical</groupId>
    <artifactId>email-service</artifactId>
    <version>1.0.0</version>
    <name>email-service</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Kafka Consumer -->
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <!-- Mail -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>

        <!-- Thymeleaf für HTML-Templates -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- JPA für email_log -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 6.3 Application Properties

```yaml
server:
  port: 8082

spring:
  application:
    name: email-service

  # Datenbank für email_log
  datasource:
    url: jdbc:postgresql://localhost:5434/email_db
    username: postgres
    password: postgres
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  # Kafka Consumer
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: email-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

  # Mail Configuration
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:deine-email@gmail.com}
    password: ${MAIL_PASSWORD:dein-app-passwort}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

app:
  kafka:
    topic:
      email-events: email-events
  mail:
    from: noreply@medical-booking.com
```

### 6.4 Kafka Consumer Configuration

```java
package com.medical.emailservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 Consumer-Threads
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
```

### 6.5 Email Event Consumer

```java
package com.medical.emailservice.messaging;

import com.medical.common.events.BookingConfirmedEvent;
import com.medical.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
        topics = "${app.kafka.topic.email-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEmailEvent(
        @Payload BookingConfirmedEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("Received event {} from partition {} offset {}", 
            event.getEventId(), partition, offset);

        try {
            emailService.sendBookingConfirmation(event);
            acknowledgment.acknowledge();
            log.info("Event {} processed successfully", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process event {}: {}", event.getEventId(), e.getMessage());
            // Optional: Dead Letter Queue (DLQ) für fehlgeschlagene Events
        }
    }
}
```

### 6.6 Email Service Implementation

```java
package com.medical.emailservice.service;

import com.medical.common.events.BookingConfirmedEvent;
import com.medical.emailservice.domain.EmailLog;
import com.medical.emailservice.domain.EmailStatus;
import com.medical.emailservice.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.mail.from}")
    private String fromAddress;

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void sendBookingConfirmation(BookingConfirmedEvent event) {
        String subject = "Terminbestätigung - Buchung #" + event.getBookingId();

        EmailLog log = EmailLog.builder()
            .bookingId(event.getBookingId())
            .recipientEmail(event.getRecipientEmail())
            .subject(subject)
            .sentAt(LocalDateTime.now())
            .status(EmailStatus.PENDING)
            .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(event.getRecipientEmail());
            message.setSubject(subject);
            message.setText(buildEmailText(event));

            mailSender.send(message);

            log.setStatus(EmailStatus.SENT);
            emailLogRepository.save(log);

            log.info("E-Mail gesendet an: {}", event.getRecipientEmail());

        } catch (Exception e) {
            log.setStatus(EmailStatus.FAILED);
            log.setErrorMessage(e.getMessage());
            emailLogRepository.save(log);

            log.error("Fehler beim E-Mail-Versand: {}", e.getMessage());
            throw new EmailException("E-Mail-Versand fehlgeschlagen", e);
        }
    }

    private String buildEmailText(BookingConfirmedEvent event) {
        return String.format("""
            Guten Tag %s,
            
            Ihre Terminbuchung wurde erfolgreich bestätigt:
            
            ═══════════════════════════════════════
            Buchungsnummer: %d
            ═══════════════════════════════════════
            
            Arzt:        %s
            Termin:      %s - %s Uhr
            
            Praxis:      %s
            Adresse:     %s
            Telefon:     %s
            
            ═══════════════════════════════════════
            
            Bitte erscheinen Sie pünktlich zu Ihrem Termin.
            Bei Verhinderung stornieren Sie bitte rechtzeitig.
            
            Mit freundlichen Grüßen,
            Ihr Terminbuchungssystem
            """,
            event.getPatientName(),
            event.getBookingId(),
            event.getDoctorName(),
            event.getAppointmentStartTime().format(FORMATTER),
            event.getAppointmentEndTime().format(FORMATTER),
            event.getPracticeName(),
            event.getPracticeAddress(),
            event.getPracticePhone()
        );
    }
}
```

### 6.7 Main Application Class

```java
package com.medical.emailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class EmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }
}
```

---

## 7. Datenbank-Schema für email_log

```sql
CREATE TABLE email_log (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_email_log_booking ON email_log(booking_id);
CREATE INDEX idx_email_log_status ON email_log(status);
CREATE INDEX idx_email_log_sent_at ON email_log(sent_at);
```

---

## 8. Deployment & Testing

### 8.1 Services starten

```bash
# 1. Kafka starten
docker-compose up -d

# 2. Patient-Customer Service starten
cd patient-customer
mvn clean install
mvn spring-boot:run

# 3. Email-Service starten (neues Terminal)
cd email-service
mvn clean install
mvn spring-boot:run
```

### 8.2 Test-Szenario

```bash
# 1. Patient registrieren
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test1234",
    "firstName": "Max",
    "lastName": "Mustermann"
  }'

# 2. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test1234"
  }'

# 3. Termin buchen
curl -X POST http://localhost:8081/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "slotId": 1
  }'
```

### 8.3 Kafka-Events prüfen

**Kafka UI öffnen:**
```bash
open http://localhost:8090
```

**Kafka CLI:**
```bash
# Topic erstellen
docker exec -it kafka kafka-topics --create \
  --bootstrap-server localhost:29092 \
  --topic email-events \
  --partitions 3 \
  --replication-factor 1

# Events lesen
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic email-events \
  --from-beginning
```

### 8.4 E-Mail-Log prüfen

```sql
-- PostgreSQL (email_db)
SELECT * FROM email_log ORDER BY sent_at DESC;

-- Fehlgeschlagene E-Mails
SELECT * FROM email_log WHERE status = 'FAILED';

-- E-Mails der letzten Stunde
SELECT * FROM email_log 
WHERE sent_at > NOW() - INTERVAL '1 hour';
```

---

## 9. Erweiterte Features

### 9.1 Dead Letter Queue (DLQ)

Für fehlgeschlagene Events:

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Object> 
    kafkaListenerContainerFactory() {
    
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    
    // DLQ konfigurieren
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        kafkaTemplate(),
        (record, ex) -> new TopicPartition("email-events-dlq", 0)
    );
    
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(
        recoverer,
        new FixedBackOff(1000L, 3L) // 3 Retries mit 1s Pause
    );
    
    factory.setCommonErrorHandler(errorHandler);
    
    return factory;
}
```

### 9.2 Scheduled Retry für FAILED E-Mails

```java
@Service
@RequiredArgsConstructor
public class EmailRetryService {

    private final EmailLogRepository emailLogRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 300000) // alle 5 Minuten
    public void retryFailedEmails() {
        List<EmailLog> failedEmails = emailLogRepository
            .findByStatusAndSentAtAfter(
                EmailStatus.FAILED,
                LocalDateTime.now().minusHours(24)
            );

        for (EmailLog log : failedEmails) {
            try {
                // Event rekonstruieren und erneut senden
                emailService.resendEmail(log.getBookingId());
            } catch (Exception e) {
                log.error("Retry fehlgeschlagen für Buchung {}", log.getBookingId());
            }
        }
    }
}
```

### 9.3 Monitoring mit Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,kafka
  metrics:
    export:
      prometheus:
        enabled: true
```

Metriken abrufen:
```bash
curl http://localhost:8082/actuator/metrics/kafka.consumer.records.consumed.total
```

---

## 10. Tabellen-Anpassungen für Email-Verifikation (Optional)

Falls du später Email-Verifikation bei Registrierung implementieren willst:

### 10.1 Änderungen an `patient`-Tabelle

```sql
ALTER TABLE patient 
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN verification_token VARCHAR(255),
ADD COLUMN verification_token_expiry TIMESTAMP;

CREATE INDEX idx_patient_verification_token ON patient(verification_token);
```

### 10.2 Neues Event: `RegistrationEvent`

```java
@Data
@Builder
public class RegistrationEvent {
    private String eventId;
    private String eventType;  // "REGISTRATION"
    private LocalDateTime timestamp;
    
    private Long patientId;
    private String email;
    private String firstName;
    private String verificationToken;
}
```

### 10.3 Verifikations-E-Mail senden

```java
private String buildVerificationEmail(RegistrationEvent event) {
    String verificationLink = String.format(
        "http://localhost:8081/api/v1/auth/verify?token=%s",
        event.getVerificationToken()
    );
    
    return String.format("""
            Guten Tag %s,
            
            willkommen bei unserem Terminbuchungssystem!
            
            Bitte bestätigen Sie Ihre E-Mail-Adresse, indem Sie auf den
            folgenden Link klicken:
            
            %s
            
            Der Link ist 24 Stunden gültig.
            
            Mit freundlichen Grüßen
            Ihr Terminbuchungssystem
            """,
        event.getFirstName(),
        verificationLink
    );
}
```

### 10.4 Verifikations-Endpoint

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PatientService patientService;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        patientService.verifyEmail(token);
        return ResponseEntity.ok("E-Mail erfolgreich verifiziert");
    }
}
```

---

## 11. Anpassungen für `practice`-Tabelle

### 11.1 Hausnummer hinzufügen

Du hast Recht – die `practice`-Tabelle sollte eine Hausnummer haben!

**Migration:**
```sql
ALTER TABLE practice 
ADD COLUMN house_number VARCHAR(20) NOT NULL DEFAULT '0';

-- Index für Adresssuche
CREATE INDEX idx_practice_address ON practice(street, house_number, city_id);
```

**Entity anpassen:**
```java
@Entity
@Table(name = "practice")
public class Practice {
    // ...existing fields...
    
    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;  // z.B. "42a", "15-17"
}
```

---

## 12. Zwischentabelle `doctor_specialty`

### Braucht sie eine ID?

**Antwort: Nein, nicht zwingend.**

#### Variante 1: Composite Primary Key (✅ Empfohlen)

```sql
CREATE TABLE doctor_specialty (
    doctor_id BIGINT NOT NULL,
    specialty_id BIGINT NOT NULL,
    PRIMARY KEY (doctor_id, specialty_id),
    CONSTRAINT fk_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctor(id) ON DELETE CASCADE,
    CONSTRAINT fk_specialty FOREIGN KEY (specialty_id) 
        REFERENCES specialty(id) ON DELETE CASCADE
);
```

**Entity:**
```java
@Entity
@Table(name = "doctor_specialty")
@IdClass(DoctorSpecialtyId.class)
public class DoctorSpecialty {
    
    @Id
    @Column(name = "doctor_id")
    private Long doctorId;
    
    @Id
    @Column(name = "specialty_id")
    private Long specialtyId;
}

@Data
public class DoctorSpecialtyId implements Serializable {
    private Long doctorId;
    private Long specialtyId;
}
```

#### Variante 2: Mit eigener ID

```sql
CREATE TABLE doctor_specialty (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    specialty_id BIGINT NOT NULL,
    UNIQUE (doctor_id, specialty_id)
);
```

**Wann sinnvoll?**
- Wenn du zusätzliche Attribute brauchst (z.B. `since_year`)
- Wenn du auf die Beziehung selbst referenzieren willst

**Für dein Projekt:** Composite Key reicht völlig aus!

---

## 13. Beziehung: doctor ↔ doctor_working_hours ↔ slot

### Fachliche Analyse

```
┌─────────────────────────────────────────────────────────────────┐
│                  Beziehungsmodell                                │
└─────────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │     doctor      │
                    │                 │
                    │ - id            │
                    │ - first_name    │
                    │ - last_name     │
                    │ - practice_id   │
                    └────────┬────────┘
                             │
                ┌────────────┴────────────┐
                │ 1                       │ 1
                │                         │
                │ n                       │ n
                ▼                         ▼
    ┌───────────────────────┐   ┌──────────────────┐
    │ doctor_working_hours  │   │      slot        │
    │                       │   │                  │
    │ - id                  │   │ - id             │
    │ - doctor_id (FK)      │   │ - doctor_id (FK) │
    │ - weekday (1-7)       │   │ - start_time     │
    │ - start_time (TIME)   │   │ - end_time       │
    │ - end_time (TIME)     │   │ - status         │
    │                       │   │                  │
    │ TEMPLATE-Daten        │   │ KONKRETE Daten   │
    │ (wiederkehrend)       │   │ (einmalig)       │
    └───────────────────────┘   └──────────────────┘
                │                         ▲
                │                         │
                └────── (generiert) ──────┘
```

### Fachliche Regeln

1. **doctor → doctor_working_hours (1:n)**
   - Ein Arzt hat mehrere Arbeitszeiten (Mo-Fr, unterschiedliche Zeiten)
   - Foreign Key: `doctor_working_hours.doctor_id → doctor.id`
   - Cascade DELETE: Arzt gelöscht → Arbeitszeiten gelöscht

2. **doctor → slot (1:n)**
   - Ein Arzt hat viele konkrete Termin-Slots
   - Foreign Key: `slot.doctor_id → doctor.id`
   - Cascade DELETE: Arzt gelöscht → Slots gelöscht

3. **doctor_working_hours → slot (Generierung)**
   - Slots werden aus Arbeitszeiten generiert
   - Ein Slot ist nur gültig, wenn er in den Arbeitszeiten liegt
   - **Keine direkte FK-Beziehung**

### Beispiel

**doctor_working_hours:**
```
doctor_id | weekday | start_time | end_time
----------|---------|------------|----------
1         | 1 (Mo)  | 09:00      | 12:00
1         | 2 (Di)  | 14:00      | 18:00
```

**Generierte Slots (für Montag 20.01.2026):**
```
doctor_id | start_time          | end_time            | status
----------|---------------------|---------------------|-------
1         | 2026-01-20 09:00:00 | 2026-01-20 10:00:00 | FREE
1         | 2026-01-20 10:00:00 | 2026-01-20 11:00:00 | FREE
1         | 2026-01-20 11:00:00 | 2026-01-20 12:00:00 | FREE
```

### SQL-Constraints

```sql
-- doctor_working_hours
ALTER TABLE doctor_working_hours
ADD CONSTRAINT fk_dwh_doctor 
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE;

-- Check: Endzeit nach Startzeit
ALTER TABLE doctor_working_hours
ADD CONSTRAINT check_time_order 
    CHECK (end_time > start_time);

-- Check: Wochentag 1-7
ALTER TABLE doctor_working_hours
ADD CONSTRAINT check_weekday 
    CHECK (weekday BETWEEN 1 AND 7);

-- slot
ALTER TABLE slot
ADD CONSTRAINT fk_slot_doctor 
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE;

-- Check: Endzeit nach Startzeit
ALTER TABLE slot
ADD CONSTRAINT check_slot_time_order 
    CHECK (end_time > start_time);
```

---

## 14. Zusammenfassung – Kafka-Architektur

### Vorteile

| Aspekt | Vorteil |
|--------|---------|
| **Skalierung** | E-Mail-Service unabhängig skalierbar |
| **Fehlertoleranz** | Events bleiben bei Service-Ausfall erhalten |
| **Entkopplung** | Services kennen sich nicht direkt |
| **Asynchronität** | Buchung blockiert nicht auf E-Mail |
| **Erweiterbarkeit** | Neue Event-Typen einfach hinzufügbar |

### Aufwand

- ❌ Kafka-Infrastruktur (Zookeeper, Broker)
- ❌ Separater Email-Service (eigenes Deployment)
- ❌ Event-Schema-Management
- ❌ Monitoring & Debugging komplexer

### Wann nutzen?

✅ **Produktionssysteme** mit hohem Durchsatz  
✅ **Mehrere Services** benötigen E-Mail-Versand  
✅ **Kritische E-Mails** (Retry-Mechanismus nötig)  
✅ **Audit-Trail** für Compliance

❌ **Nicht für** kleine Testprojekte oder Prototypen

---

## 15. Nächste Schritte

1. ✅ Entscheide: Einfache Variante oder Kafka?
2. ✅ Falls Kafka: Docker Compose starten
3. ✅ Email-Service als neues Maven-Projekt erstellen
4. ✅ Event-Model definieren
5. ✅ Producer im Consumer-Service implementieren
6. ✅ Consumer im Email-Service implementieren
7. ✅ Tests durchführen
8. ✅ Monitoring aufsetzen

**Für dein Testprojekt empfehle ich die einfache Variante aus `Email_Service_Implementation_Guide.md`!**
