# Email-Verifikation – Implementierungsleitfaden

## Überblick

Dieses Dokument beschreibt, wie Sie **Email-Verifikation** in Ihr Terminbuchungssystem integrieren können.

**Hinweis**: Dies ist **optional** und für die aktuelle Testphase **nicht erforderlich**.

---

## 1. Warum Email-Verifikation?

### Vorteile:
- ✅ **Sicherheit**: Verhindert Fake-Accounts mit fremden Email-Adressen
- ✅ **Datenqualität**: Stellt sicher, dass Emails gültig sind
- ✅ **Rechtssicherheit**: DSGVO-konform durch bestätigte Einwilligung
- ✅ **Kommunikation**: Garantiert, dass Termine ankommen

### Nachteile:
- ❌ **Komplexität**: SMTP-Server, Token-Management, Ablaufzeiten
- ❌ **User Experience**: Zusätzlicher Schritt bei Registrierung
- ❌ **Infrastruktur**: Email-Provider oder SMTP-Setup nötig

---

## 2. Architektur-Übersicht

Es gibt **zwei verschiedene Ansätze** für Email-Verifikation:

### 🔗 **Methode A: Link-basiert (klassisch)**

```
1. Patient registriert sich
   ↓
2. Account wird erstellt (is_verified = false)
   ↓
3. Verifikations-Token generiert & gespeichert
   ↓
4. Email mit Link verschickt
   ↓
5. Patient klickt auf Link
   ↓
6. Token validiert (Ablauf geprüft)
   ↓
7. Account aktiviert (is_verified = true)
   ↓
8. Patient kann sich einloggen
```

**Vorteile:**
- ✅ Einfach für User (nur Klick)
- ✅ Funktioniert auf allen Geräten

**Nachteile:**
- ❌ Link kann abgefangen werden
- ❌ Schwer auf Mobile (App vs. Browser)

---

### 🔢 **Methode B: Code-basiert (modern & sicherer)**

```
1. Patient registriert sich
   ↓
2. Account wird erstellt (is_verified = false)
   ↓
3. 6-stelliger Code generiert & gespeichert
   ↓
4. Email mit Code verschickt
   ↓
5. Patient gibt Code in App/Website ein
   ↓
6. Code validiert (Format, Ablauf, Versuche geprüft)
   ↓
7. Account aktiviert (is_verified = true)
   ↓
8. Patient kann sich einloggen
```

**Vorteile:**
- ✅ Sicherer (kein abfangbarer Link)
- ✅ Bessere Mobile-UX
- ✅ Ähnlich wie 2FA
- ✅ Code kann telefonisch durchgegeben werden

**Nachteile:**
- ❌ Zusätzliches Eingabefeld nötig
- ❌ User kann sich vertippen

---

### 📊 Empfehlung:

| Anwendungsfall | Methode A (Link) | Methode B (Code) |
|----------------|------------------|------------------|
| Web-App only | ✅ Gut | ✅ Sehr gut |
| Mobile App | ⚠️ Kompliziert | ✅ Ideal |
| Sicherheit wichtig | ⚠️ Mittel | ✅ Hoch |
| Einfachheit wichtig | ✅ Am einfachsten | ⚠️ Eingabe nötig |

**Für Ihr Projekt:** Methode B (Code) ist **moderner und für Mobile-First-Apps besser geeignet**.

Die folgende Dokumentation beschreibt **beide Methoden**.

---

## 3. Datenbank-Änderungen

### 3.1 Tabelle `patient` erweitern

#### 🔗 **Methode A: Link-basiert**

```sql
-- Email-Verifikation mit Link
ALTER TABLE patient ADD COLUMN is_verified BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE patient ADD COLUMN verification_token VARCHAR(255);
ALTER TABLE patient ADD COLUMN verification_token_expires_at TIMESTAMP;
ALTER TABLE patient ADD COLUMN updated_at TIMESTAMP;
```

#### 🔢 **Methode B: Code-basiert (EMPFOHLEN)**

```sql
-- Email-Verifikation mit Code
ALTER TABLE patient ADD COLUMN is_verified BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE patient ADD COLUMN verification_code VARCHAR(6);
ALTER TABLE patient ADD COLUMN verification_code_expires_at TIMESTAMP;
ALTER TABLE patient ADD COLUMN verification_attempts INT DEFAULT 0;
ALTER TABLE patient ADD COLUMN updated_at TIMESTAMP;
```

**Erklärung neue Felder:**
- `verification_code`: 6-stelliger numerischer Code (z.B. "123456")
- `verification_attempts`: Zählt Fehlversuche (max. 3 oder 5)
- Ablaufzeit: Typisch 15 Minuten (kürzer als Link-Methode)

### 3.2 Vollständige Patient-Tabelle

#### 🔗 **Variante A: Link-basiert**

| Attribut | Typ | Erklärung |
|----------|-----|-----------|
| id | BIGINT | Primärschlüssel |
| email | VARCHAR(255) | E-Mail-Adresse (UNIQUE) |
| first_name | VARCHAR(255) | Vorname |
| last_name | VARCHAR(255) | Nachname |
| password_hash | VARCHAR(255) | Gehashtes Passwort (BCrypt) |
| **is_verified** | **BOOLEAN** | **Email bestätigt? (default: false)** |
| **verification_token** | **VARCHAR(255)** | **UUID-Token für Link** |
| **verification_token_expires_at** | **TIMESTAMP** | **Ablaufzeit (z.B. 24h)** |
| created_at | TIMESTAMP | Registrierungszeitpunkt |
| updated_at | TIMESTAMP | Letzte Änderung |

#### 🔢 **Variante B: Code-basiert (EMPFOHLEN)**

| Attribut | Typ | Erklärung |
|----------|-----|-----------|
| id | BIGINT | Primärschlüssel |
| email | VARCHAR(255) | E-Mail-Adresse (UNIQUE) |
| first_name | VARCHAR(255) | Vorname |
| last_name | VARCHAR(255) | Nachname |
| password_hash | VARCHAR(255) | Gehashtes Passwort (BCrypt) |
| **is_verified** | **BOOLEAN** | **Email bestätigt? (default: false)** |
| **verification_code** | **VARCHAR(6)** | **6-stelliger Code** |
| **verification_code_expires_at** | **TIMESTAMP** | **Ablaufzeit (15 Min)** |
| **verification_attempts** | **INT** | **Anzahl Fehlversuche** |
| created_at | TIMESTAMP | Registrierungszeitpunkt |
| updated_at | TIMESTAMP | Letzte Änderung |

### 3.3 Index für Performance

**Link-basiert:**
```sql
CREATE INDEX idx_patient_verification_token ON patient(verification_token);
```

**Code-basiert:**
```sql
-- Kein Index nötig (Code wird nicht gesucht, nur verglichen)
-- Email ist bereits UNIQUE indexed
```

---

## 4. Backend-Implementierung (Java/Spring Boot)

### 4.1 Abhängigkeiten hinzufügen

**pom.xml:**
```xml
<!-- Email-Versand -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- UUID Generation (meist schon vorhanden) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```

### 4.2 Email-Konfiguration

**application.properties / application.yml:**

```properties
# SMTP-Konfiguration (Beispiel: Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ihre-email@gmail.com
spring.mail.password=ihr-app-passwort
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Verifikations-Link
app.verification.base-url=http://localhost:3000
app.verification.token-validity-hours=24
```

**Alternativen zu Gmail:**
- SendGrid (Professional)
- AWS SES (Amazon)
- Mailgun (Developer-freundlich)
- SMTP2GO

### 4.3 Patient Entity erweitern

#### 🔗 **Variante A: Link-basiert**

**Patient.java:**
```java
@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private String passwordHash;

    // NEU: Email-Verifikation
    @Column(nullable = false)
    private Boolean isVerified = false;

    private String verificationToken;

    private LocalDateTime verificationTokenExpiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Getter & Setter
}
```

#### 🔢 **Variante B: Code-basiert (EMPFOHLEN)**

**Patient.java:**
```java
@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private String passwordHash;

    // NEU: Email-Verifikation mit Code
    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(length = 6)
    private String verificationCode;

    private LocalDateTime verificationCodeExpiresAt;

    @Column(nullable = false)
    private Integer verificationAttempts = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Getter & Setter
}
```

### 4.4 Email-Service erstellen

#### 🔗 **Variante A: Link-basiert**

**EmailService.java:**
```java
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.verification.base-url}")
    private String baseUrl;
    
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Bestätigen Sie Ihre Email-Adresse");
            message.setText(
                "Willkommen!\n\n" +
                "Bitte bestätigen Sie Ihre Email-Adresse:\n\n" +
                baseUrl + "/api/v1/auth/verify-email?token=" + token + "\n\n" +
                "Dieser Link ist 24 Stunden gültig.\n\n" +
                "Falls Sie diese Email nicht angefordert haben, ignorieren Sie sie bitte."
            );
            
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Email-Versand fehlgeschlagen", e);
        }
    }
}
```

**Für HTML-Emails:**
```java
public void sendVerificationEmailHtml(String toEmail, String token) {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    
    helper.setTo(toEmail);
    helper.setSubject("Bestätigen Sie Ihre Email-Adresse");
    
    String htmlContent = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif;">
            <h2>Willkommen beim Terminbuchungssystem!</h2>
            <p>Bitte bestätigen Sie Ihre Email-Adresse:</p>
            <a href="%s/api/v1/auth/verify-email?token=%s"
               style="background-color: #4CAF50; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px;">
               Email bestätigen
            </a>
            <p style="margin-top: 20px; color: #666;">
               Dieser Link ist 24 Stunden gültig.
            </p>
        </body>
        </html>
        """.formatted(baseUrl, token);
    
    helper.setText(htmlContent, true);
    mailSender.send(message);
}
```

---

#### 🔢 **Variante B: Code-basiert (EMPFOHLEN)**

**EmailService.java:**
```java
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Sendet einen 6-stelligen Verifikationscode per Email
     */
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Ihr Verifikationscode");
            message.setText(
                "Willkommen!\n\n" +
                "Ihr Verifikationscode lautet:\n\n" +
                code + "\n\n" +
                "Dieser Code ist 15 Minuten gültig.\n\n" +
                "Falls Sie diese Email nicht angefordert haben, ignorieren Sie sie bitte."
            );
            
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Email-Versand fehlgeschlagen", e);
        }
    }
    
    /**
     * HTML-Version mit besserem Design
     */
    public void sendVerificationCodeHtml(String toEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setTo(toEmail);
        helper.setSubject("Ihr Verifikationscode");
        
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                    <h2 style="color: #333;">Willkommen beim Terminbuchungssystem!</h2>
                    <p style="color: #666; font-size: 16px;">
                        Bitte geben Sie folgenden Code ein, um Ihre Email-Adresse zu bestätigen:
                    </p>
                    <div style="background-color: #f0f0f0; padding: 20px; text-align: center; border-radius: 5px; margin: 20px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #4CAF50;">
                            %s
                        </span>
                    </div>
                    <p style="color: #999; font-size: 14px;">
                        Dieser Code ist 15 Minuten gültig.
                    </p>
                    <p style="color: #999; font-size: 14px;">
                        Falls Sie diese Email nicht angefordert haben, ignorieren Sie sie bitte.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(code);
        
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
```

### 4.5 Registrierungs-Logik anpassen

#### 🔗 **Variante A: Link-basiert**

**AuthService.java:**
```java
@Service
public class AuthService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.verification.token-validity-hours}")
    private int tokenValidityHours;
    
    public void registerPatient(RegisterRequest request) {
        // 1. Prüfen ob Email bereits existiert
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        
        // 2. Patient erstellen
        Patient patient = new Patient();
        patient.setEmail(request.getEmail());
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // 3. Verifikations-Token generieren
        String token = UUID.randomUUID().toString();
        patient.setVerificationToken(token);
        patient.setVerificationTokenExpiresAt(
            LocalDateTime.now().plusHours(tokenValidityHours)
        );
        patient.setIsVerified(false); // Wichtig!
        
        patient.setCreatedAt(LocalDateTime.now());
        
        // 4. Speichern
        patientRepository.save(patient);
        
        // 5. Verifikations-Email senden
        emailService.sendVerificationEmail(patient.getEmail(), token);
    }
}
```

---

#### 🔢 **Variante B: Code-basiert (EMPFOHLEN)**

**VerificationCodeGenerator.java (Helper-Klasse):**
```java
@Component
public class VerificationCodeGenerator {
    
    private final SecureRandom random = new SecureRandom();
    
    /**
     * Generiert einen 6-stelligen numerischen Code
     * @return Code als String (z.B. "123456")
     */
    public String generateCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
```

**AuthService.java:**
```java
@Service
public class AuthService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private VerificationCodeGenerator codeGenerator;
    
    @Value("${app.verification.code-validity-minutes:15}")
    private int codeValidityMinutes;
    
    public RegisterResponse registerPatient(RegisterRequest request) {
        // 1. Prüfen ob Email bereits existiert
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email bereits registriert");
        }
        
        // 2. Patient erstellen
        Patient patient = new Patient();
        patient.setEmail(request.getEmail());
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // 3. Verifikations-Code generieren
        String code = codeGenerator.generateCode();
        patient.setVerificationCode(code);
        patient.setVerificationCodeExpiresAt(
            LocalDateTime.now().plusMinutes(codeValidityMinutes)
        );
        patient.setVerificationAttempts(0);
        patient.setIsVerified(false);
        
        patient.setCreatedAt(LocalDateTime.now());
        
        // 4. Speichern
        patient = patientRepository.save(patient);
        
        // 5. Verifikations-Code per Email senden
        emailService.sendVerificationCode(patient.getEmail(), code);
        
        return new RegisterResponse(
            "Registrierung erfolgreich. Bitte prüfen Sie Ihre Emails.",
            patient.getId()
        );
    }
}
```

### 4.6 Verifikations-Endpoint

#### 🔗 **Variante A: Link-basiert**

**AuthController.java:**
```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email erfolgreich bestätigt!");
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam String email) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok("Neue Verifikations-Email wurde versendet.");
    }
}
```

**AuthService.java (erweitern):**
```java
@Transactional
public void verifyEmail(String token) {
    // 1. Patient anhand Token finden
    Patient patient = patientRepository.findByVerificationToken(token)
        .orElseThrow(() -> new InvalidTokenException("Ungültiger Token"));
    
    // 2. Prüfen ob bereits verifiziert
    if (patient.getIsVerified()) {
        throw new AlreadyVerifiedException("Email bereits bestätigt");
    }
    
    // 3. Prüfen ob Token abgelaufen
    if (LocalDateTime.now().isAfter(patient.getVerificationTokenExpiresAt())) {
        throw new TokenExpiredException("Token ist abgelaufen");
    }
    
    // 4. Account aktivieren
    patient.setIsVerified(true);
    patient.setVerificationToken(null); // Token löschen
    patient.setVerificationTokenExpiresAt(null);
    patient.setUpdatedAt(LocalDateTime.now());
    
    patientRepository.save(patient);
}

@Transactional
public void resendVerificationEmail(String email) {
    Patient patient = patientRepository.findByEmail(email)
        .orElseThrow(() -> new PatientNotFoundException());
    
    if (patient.getIsVerified()) {
        throw new AlreadyVerifiedException();
    }
    
    // Neuen Token generieren
    String newToken = UUID.randomUUID().toString();
    patient.setVerificationToken(newToken);
    patient.setVerificationTokenExpiresAt(
        LocalDateTime.now().plusHours(tokenValidityHours)
    );
    
    patientRepository.save(patient);
    
    // Email erneut senden
    emailService.sendVerificationEmail(email, newToken);
}
```

---

#### 🔢 **Variante B: Code-basiert (EMPFOHLEN)**

**VerifyCodeRequest.java (DTO):**
```java
public class VerifyCodeRequest {
    @Email(message = "Ungültige Email-Adresse")
    @NotBlank(message = "Email ist erforderlich")
    private String email;
    
    @NotBlank(message = "Code ist erforderlich")
    @Pattern(regexp = "^\\d{6}$", message = "Code muss 6 Ziffern haben")
    private String code;
    
    // Getter & Setter
}
```

**AuthController.java:**
```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Verifiziert Email mit eingegebenem Code
     */
    @PostMapping("/verify-code")
    public ResponseEntity<MessageResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new MessageResponse("Email erfolgreich bestätigt!"));
    }
    
    /**
     * Sendet neuen Verifikationscode
     */
    @PostMapping("/resend-code")
    public ResponseEntity<MessageResponse> resendCode(@RequestParam String email) {
        authService.resendVerificationCode(email);
        return ResponseEntity.ok(new MessageResponse("Neuer Code wurde versendet."));
    }
}
```

**AuthService.java (erweitern):**
```java
@Transactional
public void verifyCode(String email, String code) {
    // 1. Patient finden
    Patient patient = patientRepository.findByEmail(email)
        .orElseThrow(() -> new InvalidCredentialsException("Ungültige Daten"));
    
    // 2. Prüfen ob bereits verifiziert
    if (patient.getIsVerified()) {
        throw new AlreadyVerifiedException("Email bereits bestätigt");
    }
    
    // 3. Prüfen ob Code abgelaufen
    if (LocalDateTime.now().isAfter(patient.getVerificationCodeExpiresAt())) {
        throw new CodeExpiredException("Code ist abgelaufen. Bitte fordern Sie einen neuen an.");
    }
    
    // 4. Prüfen: Zu viele Fehlversuche?
    if (patient.getVerificationAttempts() >= 5) {
        throw new TooManyAttemptsException("Zu viele Fehlversuche. Bitte fordern Sie einen neuen Code an.");
    }
    
    // 5. Code vergleichen (Timing-Safe)
    if (!MessageDigest.isEqual(code.getBytes(), patient.getVerificationCode().getBytes())) {
        // Fehlversuch zählen
        patient.setVerificationAttempts(patient.getVerificationAttempts() + 1);
        patientRepository.save(patient);
        
        int remainingAttempts = 5 - patient.getVerificationAttempts();
        throw new InvalidCodeException(
            "Falscher Code. Noch " + remainingAttempts + " Versuche übrig."
        );
    }
    
    // 6. Account aktivieren
    patient.setIsVerified(true);
    patient.setVerificationCode(null); // Code löschen
    patient.setVerificationCodeExpiresAt(null);
    patient.setVerificationAttempts(0);
    patient.setUpdatedAt(LocalDateTime.now());
    
    patientRepository.save(patient);
}

@Transactional
public void resendVerificationCode(String email) {
    // 1. Patient finden
    Patient patient = patientRepository.findByEmail(email)
        .orElseThrow(() -> new PatientNotFoundException("Patient nicht gefunden"));
    
    // 2. Prüfen ob bereits verifiziert
    if (patient.getIsVerified()) {
        throw new AlreadyVerifiedException("Email bereits bestätigt");
    }
    
    // 3. Rate Limiting (optional, siehe Abschnitt 8.2)
    // rateLimitService.checkResendRateLimit(email);
    
    // 4. Neuen Code generieren
    String newCode = codeGenerator.generateCode();
    patient.setVerificationCode(newCode);
    patient.setVerificationCodeExpiresAt(
        LocalDateTime.now().plusMinutes(codeValidityMinutes)
    );
    patient.setVerificationAttempts(0); // Zurücksetzen
    patient.setUpdatedAt(LocalDateTime.now());
    
    patientRepository.save(patient);
    
    // 5. Code per Email senden
    emailService.sendVerificationCode(email, newCode);
}
```

### 4.7 Login-Logik anpassen

**AuthService.java:**
```java
public LoginResponse login(LoginRequest request) {
    // 1. Patient finden
    Patient patient = patientRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new InvalidCredentialsException());
    
    // 2. Passwort prüfen
    if (!passwordEncoder.matches(request.getPassword(), patient.getPasswordHash())) {
        throw new InvalidCredentialsException();
    }
    
    // 3. NEU: Prüfen ob verifiziert
    if (!patient.getIsVerified()) {
        throw new EmailNotVerifiedException("Bitte bestätigen Sie zuerst Ihre Email-Adresse");
    }
    
    // 4. JWT generieren
    String token = jwtService.generateToken(patient.getId());
    
    return new LoginResponse(token, 3600, patient.getId());
}
```

### 4.8 Repository erweitern

**PatientRepository.java:**
```java
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    // NEU für Email-Verifikation
    Optional<Patient> findByVerificationToken(String token);
}
```

---

## 5. API-Endpoints Übersicht

### 5.1 Registrierung (geändert)

**Beide Methoden identisch:**
```
POST /api/v1/auth/register

Request:
{
  "email": "patient@example.com",
  "password": "securePassword123",
  "firstName": "Max",
  "lastName": "Mustermann"
}

Response (201 Created):
{
  "message": "Registrierung erfolgreich. Bitte prüfen Sie Ihre Emails.",
  "userId": 1
}
```

---

### 5.2 Email-Verifikation

#### 🔗 **Methode A: Link-basiert**

```
GET /api/v1/auth/verify-email?token={uuid-token}

Response (200 OK):
{
  "message": "Email erfolgreich bestätigt!"
}

Fehler (400 Bad Request):
{
  "error": "Token ist abgelaufen"
}
```

#### 🔢 **Methode B: Code-basiert (EMPFOHLEN)**

```
POST /api/v1/auth/verify-code

Request:
{
  "email": "patient@example.com",
  "code": "123456"
}

Response (200 OK):
{
  "message": "Email erfolgreich bestätigt!"
}

Fehler (400 Bad Request):
{
  "error": "Falscher Code. Noch 4 Versuche übrig."
}

Fehler (400 Bad Request):
{
  "error": "Code ist abgelaufen. Bitte fordern Sie einen neuen an."
}

Fehler (429 Too Many Requests):
{
  "error": "Zu viele Fehlversuche. Bitte fordern Sie einen neuen Code an."
}
```

---

### 5.3 Erneutes Senden

#### 🔗 **Methode A: Link**
```
POST /api/v1/auth/resend-verification?email=patient@example.com

Response (200 OK):
{
  "message": "Neue Verifikations-Email wurde versendet."
}
```

#### 🔢 **Methode B: Code**
```
POST /api/v1/auth/resend-code?email=patient@example.com

Response (200 OK):
{
  "message": "Neuer Code wurde versendet."
}
```

---

### 5.4 Login (geändert)

**Beide Methoden identisch:**
```
POST /api/v1/auth/login

Request:
{
  "email": "patient@example.com",
  "password": "securePassword123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "userId": 1
}

Fehler (403 Forbidden):
{
  "error": "Bitte bestätigen Sie zuerst Ihre Email-Adresse"
}
```

---

## 6. Frontend-Integration

### 6.1 Registrierungs-Flow

**Nach erfolgreicher Registrierung (beide Methoden):**
```javascript
async function register(userData) {
  const response = await fetch('/api/v1/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData)
  });
  
  if (response.ok) {
    const data = await response.json();
    
    // Für Code-Methode: Zur Verifikations-Seite weiterleiten
    router.push(`/verify-code?email=${userData.email}`);
    
    // Für Link-Methode: Nur Hinweis anzeigen
    // showMessage('Bitte prüfen Sie Ihre Emails.');
    // router.push('/login');
  }
}
```

---

### 6.2 Verifikations-Seite

#### 🔗 **Methode A: Link-basiert**

**VerifyEmail.vue / VerifyEmail.jsx:**
```javascript
// URL: /verify-email?token=abc-123

async function verifyEmail() {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');
  
  try {
    const response = await fetch(`/api/v1/auth/verify-email?token=${token}`);
    
    if (response.ok) {
      showSuccess('Email erfolgreich bestätigt! Sie können sich jetzt anmelden.');
      setTimeout(() => router.push('/login'), 3000);
    } else {
      const error = await response.json();
      showError(error.message || 'Verifikation fehlgeschlagen');
    }
  } catch (error) {
    showError('Ein Fehler ist aufgetreten');
  }
}

// Beim Laden der Seite ausführen
onMounted(verifyEmail);
```

---

#### 🔢 **Methode B: Code-basiert (EMPFOHLEN)**

**VerifyCode.vue / VerifyCode.jsx:**
```javascript
// URL: /verify-code?email=patient@example.com

import { ref, onMounted } from 'vue';

const email = ref('');
const code = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  email.value = urlParams.get('email') || '';
});

async function verifyCode() {
  if (code.value.length !== 6) {
    errorMessage.value = 'Code muss 6 Ziffern haben';
    return;
  }
  
  isLoading.value = true;
  errorMessage.value = '';
  
  try {
    const response = await fetch('/api/v1/auth/verify-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: email.value,
        code: code.value
      })
    });
    
    if (response.ok) {
      showSuccess('Email erfolgreich bestätigt!');
      setTimeout(() => router.push('/login'), 2000);
    } else {
      const error = await response.json();
      errorMessage.value = error.error || 'Verifikation fehlgeschlagen';
      
      // Bei zu vielen Versuchen: Resend-Button anzeigen
      if (response.status === 429) {
        showResendButton();
      }
    }
  } catch (error) {
    errorMessage.value = 'Ein Fehler ist aufgetreten';
  } finally {
    isLoading.value = false;
  }
}

// Automatische Formatierung: Nur Zahlen
function handleInput(event) {
  code.value = event.target.value.replace(/\D/g, '').substring(0, 6);
}
```

**HTML-Template (Vue):**
```html
<template>
  <div class="verify-container">
    <h2>Email-Adresse bestätigen</h2>
    <p>Wir haben einen 6-stelligen Code an {{ email }} gesendet.</p>
    
    <form @submit.prevent="verifyCode">
      <input
        v-model="code"
        @input="handleInput"
        type="text"
        inputmode="numeric"
        maxlength="6"
        placeholder="123456"
        class="code-input"
        :disabled="isLoading"
      />
      
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      
      <button type="submit" :disabled="isLoading || code.length !== 6">
        {{ isLoading ? 'Wird geprüft...' : 'Bestätigen' }}
      </button>
    </form>
    
    <button @click="resendCode" class="resend-button">
      Code erneut senden
    </button>
  </div>
</template>

<style scoped>
.code-input {
  font-size: 24px;
  letter-spacing: 8px;
  text-align: center;
  padding: 15px;
  width: 200px;
  border: 2px solid #ddd;
  border-radius: 5px;
}

.error {
  color: red;
  margin: 10px 0;
}

.resend-button {
  margin-top: 20px;
  background: transparent;
  color: #4CAF50;
  border: none;
  text-decoration: underline;
  cursor: pointer;
}
</style>
```

---

### 6.3 Erneutes Senden

#### 🔗 **Methode A: Link**
```javascript
async function resendVerification(email) {
  const response = await fetch(
    `/api/v1/auth/resend-verification?email=${email}`,
    { method: 'POST' }
  );
  
  if (response.ok) {
    showMessage('Neue Verifikations-Email wurde versendet.');
  }
}
```

#### 🔢 **Methode B: Code**
```javascript
async function resendCode() {
  isLoading.value = true;
  
  try {
    const response = await fetch(
      `/api/v1/auth/resend-code?email=${email.value}`,
      { method: 'POST' }
    );
    
    if (response.ok) {
      showSuccess('Neuer Code wurde versendet. Bitte prüfen Sie Ihre Emails.');
      code.value = ''; // Eingabefeld leeren
      errorMessage.value = '';
    } else {
      const error = await response.json();
      errorMessage.value = error.error || 'Fehler beim Senden';
    }
  } catch (error) {
    errorMessage.value = 'Ein Fehler ist aufgetreten';
  } finally {
    isLoading.value = false;
  }
}
```

---

### 6.4 Login mit Fehlerbehandlung

**Beide Methoden identisch:**
```javascript
async function login(credentials) {
  try {
    const response = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    
    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('token', data.token);
      router.push('/dashboard');
    } else if (response.status === 403) {
      // Email nicht verifiziert
      showError('Bitte bestätigen Sie zuerst Ihre Email-Adresse');
      
      // Für Code-Methode: Direkt zur Verifikation
      router.push(`/verify-code?email=${credentials.email}`);
      
      // Für Link-Methode: Resend-Button anzeigen
      // showResendButton(credentials.email);
    } else {
      showError('Login fehlgeschlagen');
    }
  } catch (error) {
    showError('Ein Fehler ist aufgetreten');
  }
}
```

---

### 6.5 Erweiterte UX-Features (Code-Methode)

**Auto-Submit nach 6. Ziffer:**
```javascript
function handleInput(event) {
  code.value = event.target.value.replace(/\D/g, '').substring(0, 6);
  
  // Automatisch submitten wenn 6 Ziffern eingegeben
  if (code.value.length === 6) {
    verifyCode();
  }
}
```

**Countdown-Timer:**
```javascript
const timeRemaining = ref(900); // 15 Minuten in Sekunden

onMounted(() => {
  const interval = setInterval(() => {
    timeRemaining.value--;
    if (timeRemaining.value <= 0) {
      clearInterval(interval);
      errorMessage.value = 'Code ist abgelaufen. Bitte fordern Sie einen neuen an.';
    }
  }, 1000);
});

// Im Template anzeigen:
// {{ Math.floor(timeRemaining / 60) }}:{{ (timeRemaining % 60).toString().padStart(2, '0') }}
```

---

## 7. Testing

### 7.1 Manuelle Tests

**Testfälle:**
1. ✅ Registrierung → Email kommt an
2. ✅ Link klicken → Account aktiviert
3. ✅ Login vor Verifikation → Fehler
4. ✅ Login nach Verifikation → Erfolg
5. ✅ Abgelaufener Token → Fehler
6. ✅ Erneutes Senden → Neue Email
7. ✅ Bereits verifiziert → Hinweis

### 7.2 Unit Tests (Beispiel)

```java
@Test
void shouldNotAllowLoginBeforeEmailVerification() {
    // Given
    Patient patient = createUnverifiedPatient();
    LoginRequest request = new LoginRequest(patient.getEmail(), "password");
    
    // When & Then
    assertThrows(EmailNotVerifiedException.class, 
        () -> authService.login(request));
}

@Test
void shouldVerifyEmailSuccessfully() {
    // Given
    Patient patient = createUnverifiedPatient();
    String token = patient.getVerificationToken();
    
    // When
    authService.verifyEmail(token);
    
    // Then
    Patient verified = patientRepository.findById(patient.getId()).get();
    assertTrue(verified.getIsVerified());
    assertNull(verified.getVerificationToken());
}

@Test
void shouldRejectExpiredToken() {
    // Given
    Patient patient = createPatientWithExpiredToken();
    
    // When & Then
    assertThrows(TokenExpiredException.class,
        () -> authService.verifyEmail(patient.getVerificationToken()));
}
```

---

## 8. Sicherheitsaspekte

### 8.1 Best Practices

✅ **Token-Sicherheit:**
- UUID verwenden (nicht vorhersagbar)
- Ablaufzeit setzen (24h empfohlen)
- Token nach Verwendung löschen

✅ **Email-Sicherheit:**
- SMTP über TLS/SSL
- App-Passwörter statt Haupt-Passwort
- Rate Limiting für Resend-Funktion

✅ **Privacy:**
- Nicht verraten ob Email existiert (bei Resend)
- Keine sensiblen Daten in Email-Links
- DSGVO-konforme Email-Texte

### 8.2 Rate Limiting

**Verhindert Spam:**
```java
@Service
public class RateLimitService {
    private final Map<String, LocalDateTime> lastResendTime = new ConcurrentHashMap<>();
    
    public void checkResendRateLimit(String email) {
        LocalDateTime lastTime = lastResendTime.get(email);
        if (lastTime != null && 
            LocalDateTime.now().isBefore(lastTime.plusMinutes(5))) {
            throw new TooManyRequestsException(
                "Bitte warten Sie 5 Minuten vor erneutem Versenden"
            );
        }
        lastResendTime.put(email, LocalDateTime.now());
    }
}
```

---

## 9. Troubleshooting

### Problem: Emails kommen nicht an

**Lösung:**
1. SMTP-Credentials prüfen
2. Firewall/Port 587 prüfen
3. Spam-Ordner kontrollieren
4. Email-Provider-Logs checken
5. TLS/SSL-Konfiguration prüfen

### Problem: Gmail blockiert

**Lösung:**
- App-Passwort generieren (nicht Haupt-Passwort)
- "Weniger sichere Apps" aktivieren (nicht empfohlen)
- OAuth2 verwenden (empfohlen)

### Problem: Token expired sofort

**Lösung:**
- Timezone überprüfen
- `LocalDateTime.now()` vs. UTC

---

## 10. Alternativen & Erweiterungen

### 10.1 OAuth2 statt SMTP

**Vorteile:**
- Sicherer
- Keine Passwörter im Code
- Bessere Zustellraten

**Provider:**
- Google OAuth2
- Microsoft Graph API

### 10.2 Externe Email-Services

**SendGrid:**
```properties
spring.sendgrid.api-key=YOUR_API_KEY
```

**AWS SES:**
```properties
cloud.aws.credentials.access-key=YOUR_KEY
cloud.aws.credentials.secret-key=YOUR_SECRET
cloud.aws.region.static=eu-central-1
```

### 10.3 Magic Links (passwortlos)

Statt Passwort → Email mit Login-Link

**Vorteile:**
- Kein Passwort vergessen
- Sicherer
- Bessere UX

---

## 11. Migrations-Checkliste

Wenn Sie Email-Verifikation später hinzufügen:

- [ ] Datenbank-Migrationen ausführen
- [ ] Patient Entity erweitern
- [ ] Email-Dependencies hinzufügen
- [ ] SMTP konfigurieren
- [ ] EmailService implementieren
- [ ] AuthService anpassen
- [ ] Neue Endpoints hinzufügen
- [ ] Frontend anpassen
- [ ] Tests schreiben
- [ ] Dokumentation aktualisieren
- [ ] Bestehende User migrieren (is_verified = true setzen)

**Bestehende Accounts aktivieren:**
```sql
-- Alle existierenden Accounts verifizieren
UPDATE patient SET is_verified = TRUE WHERE created_at < NOW();
```

---

## 12. Zusammenfassung

### Aufwand: ~8-16 Stunden

**Datenbank:** ~1h  
**Backend:** ~4-8h  
**Frontend:** ~2-4h  
**Testing:** ~2-4h

### ROI (Return on Investment):

**Für Testprojekt:** ❌ Zu aufwändig  
**Für MVP:** ⚠️ Optional  
**Für Produktion:** ✅ Empfohlen

---

**Hinweis:** Dies ist eine vollständige Implementierungsanleitung.  
Bewahren Sie diese Datei für spätere Verwendung auf!

