# E-Mail-Versand im Terminbuchungssystem – Implementierungsleitfaden

## 1. Architekturentscheidung: Wo gehört der E-Mail-Versand hin?

### Option 1: E-Mail-Versand im Consumer-Service (✅ Empfohlen für dein Testprojekt)

**Vorteile:**
- ✅ Einfacher für Testprojekte
- ✅ Keine zusätzliche Service-Komplexität
- ✅ Direkter Zugriff auf Buchungsdaten
- ✅ Schneller zu implementieren
- ✅ Einfacher lokal zu testen

**Nachteile:**
- ❌ Consumer-Service hat mehr Verantwortung
- ❌ Schwieriger zu skalieren bei vielen E-Mails
- ❌ Keine Wiederverwendbarkeit für andere Services

**Architektur:**
```
┌─────────────────────────────────────┐
│     Consumer-Service                │
│                                     │
│  ┌──────────────┐                  │
│  │ BookingService│                 │
│  └───────┬───────┘                  │
│          │                          │
│          ▼                          │
│  ┌──────────────┐                  │
│  │ EmailService │                  │
│  └───────┬───────┘                  │
│          │                          │
│          ▼                          │
│  ┌──────────────┐                  │
│  │ JavaMailSender│                 │
│  └───────┬───────┘                  │
└──────────┼──────────────────────────┘
           │
           ▼
    Gmail SMTP Server
```

---

### Option 2: Separater E-Mail-Service mit Kafka

**Vorteile:**
- ✅ Separation of Concerns
- ✅ Wiederverwendbar für andere Services
- ✅ Unabhängig skalierbar
- ✅ Asynchrone Verarbeitung
- ✅ Retry-Mechanismus bei Fehlern
- ✅ Produktionsreif

**Nachteile:**
- ❌ Mehr Komplexität (Kafka-Infrastruktur nötig)
- ❌ Mehr Aufwand für Entwicklung & Deployment
- ❌ Überdimensioniert für Testprojekt
- ❌ Schwieriger lokal zu testen

**Architektur:**
```
┌─────────────────┐        ┌─────────────────┐        ┌─────────────────┐
│ Consumer-Service│        │  Kafka Broker   │        │  Email-Service  │
│                 │        │                 │        │                 │
│ ┌─────────────┐ │        │ ┌─────────────┐ │        │ ┌─────────────┐ │
│ │BookingService│ │ ─────► │ │   Topic:    │ │ ─────► │ │   Kafka     │ │
│ └─────────────┘ │ publish│ │ email-events│ │consume │ │  Consumer   │ │
│                 │        │ └─────────────┘ │        │ └──────┬──────┘ │
└─────────────────┘        └─────────────────┘        │        │        │
                                                       │        ▼        │
                                                       │ ┌─────────────┐ │
                                                       │ │EmailService │ │
                                                       │ └──────┬──────┘ │
                                                       │        │        │
                                                       │        ▼        │
                                                       │ ┌─────────────┐ │
                                                       │ │MailSender   │ │
                                                       │ └──────┬──────┘ │
                                                       └────────┼────────┘
                                                                │
                                                                ▼
                                                         Gmail SMTP Server
```

---

### 🎯 Empfehlung für dein Projekt:

**E-Mail-Versand im Consumer-Service implementieren**

**Grund:** 
- Du willst das System lokal testen
- Ein separater Service würde zusätzliche Infrastruktur (Kafka) erfordern
- Für ein Testprojekt ist die einfache Variante ausreichend
- Du kannst später jederzeit auf einen separaten Service migrieren

---

## 2. Implementierung im Consumer-Service (Einfache Variante)

### 2.1 Benötigte Dependencies

Füge in `patient-customer/pom.xml` hinzu:

```xml
<dependencies>
    <!-- E-Mail-Versand -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- Optional: HTML-E-Mails mit Thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
</dependencies>
```

---

### 2.2 E-Mail-Konfiguration

#### Schritt 1: Gmail App-Passwort erstellen

1. Gehe zu [myaccount.google.com](https://myaccount.google.com)
2. Wähle **Sicherheit**
3. Aktiviere **Bestätigung in zwei Schritten** (falls noch nicht aktiv)
4. Suche nach **App-Passwörter**
5. Wähle **Mail** und **Mac** (oder anderes Gerät)
6. Kopiere das generierte 16-stellige Passwort

#### Schritt 2: `application.yml` konfigurieren

Erstelle `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: patient-customer-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/customer_db
    username: postgres
    password: postgres
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
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
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

# App-spezifische Konfiguration
app:
  mail:
    from: noreply@medical-booking.com
    enabled: true
```

#### Schritt 3: Lokale Konfiguration (nicht in Git committen!)

Erstelle `src/main/resources/application-local.yml`:

```yaml
spring:
  mail:
    username: deine-private-email@gmail.com
    password: xxxx xxxx xxxx xxxx  # Dein 16-stelliges App-Passwort

app:
  mail:
    enabled: true
```

Füge in `.gitignore` hinzu:
```
application-local.yml
```

---

### 2.3 Domain Model für E-Mail-Daten

#### DTO: `BookingConfirmationData.java`

```java
package test.patient_customer.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingConfirmationData {
    private Long bookingId;
    private String recipientEmail;
    private String patientName;
    private String doctorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String practiceName;
    private String practiceAddress;
    private String practicePhone;
}
```

---

### 2.4 E-Mail-Service Interface

```java
package test.patient_customer.application.port.out;

import test.patient_customer.domain.model.BookingConfirmationData;

public interface EmailService {
    /**
     * Sendet eine Buchungsbestätigung per E-Mail
     * @param data Buchungsinformationen
     */
    void sendBookingConfirmation(BookingConfirmationData data);
}
```

---

### 2.5 E-Mail-Service Implementierung (Einfache Text-E-Mail)

```java
package test.patient_customer.infrastructure.adapter.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import test.patient_customer.application.port.out.EmailService;
import test.patient_customer.domain.model.BookingConfirmationData;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.enabled}")
    private boolean emailEnabled;

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void sendBookingConfirmation(BookingConfirmationData data) {
        if (!emailEnabled) {
            log.info("E-Mail-Versand deaktiviert (Testmodus)");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(data.getRecipientEmail());
            message.setSubject("Terminbestätigung - Buchung #" + data.getBookingId());
            message.setText(buildEmailText(data));

            mailSender.send(message);
            log.info("Bestätigungs-E-Mail gesendet an: {}", data.getRecipientEmail());

        } catch (Exception e) {
            log.error("Fehler beim E-Mail-Versand an {}: {}", 
                     data.getRecipientEmail(), e.getMessage());
            // In Produktionsumgebung: Exception werfen oder Retry-Logik
        }
    }

    private String buildEmailText(BookingConfirmationData data) {
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
            
            ---
            Diese E-Mail wurde automatisch generiert.
            Bitte nicht auf diese E-Mail antworten.
            """,
            data.getPatientName(),
            data.getBookingId(),
            data.getDoctorName(),
            data.getStartTime().format(FORMATTER),
            data.getEndTime().format(FORMATTER),
            data.getPracticeName(),
            data.getPracticeAddress(),
            data.getPracticePhone()
        );
    }
}
```

---

### 2.6 Integration in BookingService

```java
package test.patient_customer.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.patient_customer.application.port.out.EmailService;
import test.patient_customer.domain.model.BookingConfirmationData;
import test.patient_customer.domain.entity.Booking;
import test.patient_customer.domain.entity.Patient;
import test.patient_customer.domain.enums.BookingStatus;
import test.patient_customer.infrastructure.repository.BookingRepository;
import test.patient_customer.infrastructure.repository.PatientRepository;
import test.patient_customer.infrastructure.client.ProviderClient;
import test.patient_customer.infrastructure.client.dto.SlotDto;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PatientRepository patientRepository;
    private final ProviderClient providerClient;
    private final EmailService emailService;

    @Transactional
    public BookingResponse createBooking(Long patientId, Long slotId) {
        log.info("Erstelle Buchung für Patient {} und Slot {}", patientId, slotId);

        // 1. Patient laden
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new EntityNotFoundException("Patient nicht gefunden"));

        // 2. Slot beim Provider-Service buchen (Feign Client)
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

        // 4. Bestätigungs-E-Mail versenden
        BookingConfirmationData emailData = BookingConfirmationData.builder()
            .bookingId(booking.getId())
            .recipientEmail(patient.getEmail())
            .patientName(patient.getFirstName() + " " + patient.getLastName())
            .doctorName(slot.getDoctorName())
            .startTime(slot.getStartTime())
            .endTime(slot.getEndTime())
            .practiceName(slot.getPracticeName())
            .practiceAddress(slot.getPracticeAddress())
            .practicePhone(slot.getPracticePhone())
            .build();

        emailService.sendBookingConfirmation(emailData);

        log.info("Buchung {} erfolgreich erstellt", booking.getId());

        return BookingMapper.toResponse(booking);
    }
}
```

---

### 2.7 Lokaler Test

#### Schritt 1: Service starten

```bash
cd patient-customer
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### Schritt 2: Registrierung

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "deine-email@gmail.com",
    "password": "test1234",
    "firstName": "Max",
    "lastName": "Mustermann"
  }'
```

#### Schritt 3: Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "deine-email@gmail.com",
    "password": "test1234"
  }'
```

Kopiere den erhaltenen JWT-Token.

#### Schritt 4: Termin buchen

```bash
curl -X POST http://localhost:8081/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <DEIN_TOKEN>" \
  -d '{
    "slotId": 1
  }'
```

#### Schritt 5: E-Mail prüfen

Öffne dein Gmail-Postfach – du solltest eine Bestätigungs-E-Mail erhalten haben.

---

### 2.8 Alternative: MailHog für lokale Tests ohne echte E-Mails

**Installation:**

```bash
# macOS
brew install mailhog

# Docker
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

**Konfiguration (`application-local.yml`):**

```yaml
spring:
  mail:
    host: localhost
    port: 1025
    username: test
    password: test
    properties:
      mail:
        smtp:
          auth: false
          starttls:
            enable: false
```

**Verwendung:**

1. MailHog starten: `mailhog`
2. Service starten
3. Buchung durchführen
4. Web-UI öffnen: [http://localhost:8025](http://localhost:8025)
5. Alle versendeten E-Mails sind dort sichtbar (keine echten E-Mails!)

---

## 3. Optional: E-Mail-Logging in Datenbank

### 3.1 Tabelle `email_log`

```sql
CREATE TABLE email_log (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    CONSTRAINT fk_email_log_booking FOREIGN KEY (booking_id) 
        REFERENCES booking(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_log_booking ON email_log(booking_id);
CREATE INDEX idx_email_log_status ON email_log(status);
```

### 3.2 Entity `EmailLog.java`

```java
package test.patient_customer.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.patient_customer.domain.enums.EmailStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "subject")
    private String subject;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
```

### 3.3 Enum `EmailStatus.java`

```java
package test.patient_customer.domain.enums;

public enum EmailStatus {
    SENT,
    FAILED,
    PENDING
}
```

### 3.4 Service mit Logging erweitern

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Override
    public void sendBookingConfirmation(BookingConfirmationData data) {
        String subject = "Terminbestätigung - Buchung #" + data.getBookingId();
        
        EmailLog log = EmailLog.builder()
            .bookingId(data.getBookingId())
            .recipientEmail(data.getRecipientEmail())
            .subject(subject)
            .sentAt(LocalDateTime.now())
            .status(EmailStatus.PENDING)
            .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(data.getRecipientEmail());
            message.setSubject(subject);
            message.setText(buildEmailText(data));

            mailSender.send(message);
            
            log.setStatus(EmailStatus.SENT);
            emailLogRepository.save(log);
            
            log.info("E-Mail erfolgreich gesendet an: {}", data.getRecipientEmail());

        } catch (Exception e) {
            log.setStatus(EmailStatus.FAILED);
            log.setErrorMessage(e.getMessage());
            emailLogRepository.save(log);
            
            log.error("Fehler beim E-Mail-Versand: {}", e.getMessage());
        }
    }
}
```

---

## 4. Zusammenfassung – Einfache Implementierung

### Was du brauchst:

1. ✅ `spring-boot-starter-mail` Dependency
2. ✅ Gmail-Konto mit App-Passwort
3. ✅ `application.yml` Konfiguration
4. ✅ `EmailService` Interface + Implementierung
5. ✅ Integration in `BookingService`
6. ✅ Optional: `email_log` Tabelle

### Vorteile dieser Lösung:

- ✅ Einfach zu implementieren
- ✅ Lokal testbar mit deiner eigenen E-Mail
- ✅ Keine zusätzliche Infrastruktur nötig
- ✅ Ausreichend für Testprojekte

### Einschränkungen:

- ❌ Synchroner E-Mail-Versand (blockiert Request)
- ❌ Kein automatischer Retry bei Fehlern
- ❌ Nicht wiederverwendbar für andere Services

---

## 5. Nächste Schritte

1. App-Passwort in Gmail erstellen
2. `application-local.yml` mit deinen Credentials erstellen
3. `EmailService` implementieren
4. In `BookingService` einbinden
5. Lokalen Test durchführen
6. E-Mail im Postfach prüfen

**Wenn du später einen separaten E-Mail-Service mit Kafka bauen willst**, lies die Datei **`Email_Service_With_Kafka.md`**!
