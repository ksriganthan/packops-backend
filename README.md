# PackOps Backend

Spring-Boot-Backend für **PackOps** – eine Webanwendung zur Simulation eines
industriellen Wiege- und Abpackprozesses.

Studierendenprojekt im Modul **IT-Projekt**
FHNW Hochschule für Wirtschaft, Olten – FS 2026

Zugehöriges Frontend: [teodorglisic/packops-frontend](https://github.com/teodorglisic/packops-frontend)
Gesamtübersicht und Bedienungsanleitung: [ksriganthan/packops](https://github.com/ksriganthan/packops)

---

## Überblick

Das Backend bildet das Verhalten der **Newtec Weighing Machine 2008PCM** mit
Memory Pans softwareseitig nach. Es stellt dem browserbasierten Client eine
REST-API bereit und übernimmt drei Aufgabenblöcke:

1. **Simulation** – Materialfluss, Gewichtserfassung, Kombinationsermittlung und
   Paketbildung über acht parallele Kanäle
2. **Fachliche Verwaltung** – Produktkatalog, Benutzer, Prozesse, Statistiken,
   Audit-Logs
3. **Sicherheit** – Anmeldung, JWT-Ausgabe, rollenbasierte Zugriffskontrolle

Die Kommunikation mit dem Frontend erfolgt ausschliesslich über REST-Endpunkte
unter `/api/**`. Nach dem Login hängt der Client ein Bearer Token an jede
Anfrage.

---

## Architektur

Klassische Schichtung, ergänzt um ein eigenständiges Simulations-Package:

```
  HTTP (Frontend)
        │
        ▼
  ┌───────────────┐   REST-Mapping, Statuscodes, @ExceptionHandler
  │  controller   │   – keine Fachlogik
  └───────┬───────┘
          ▼
  ┌───────────────┐   Fachlogik, Validierung, Mapping DTO ↔ Domain
  │   service     │◄──────────────┐
  └───────┬───────┘               │
          ▼                       │
  ┌───────────────┐        ┌──────┴────────┐
  │  repository   │        │  simulation   │  Wiegeanlage, Buckets,
  │  (Spring Data)│        │               │  Kombinationslogik, Ticks
  └───────┬───────┘        └───────────────┘
          ▼
  ┌───────────────┐
  │  H2 / Postgres│
  └───────────────┘

  security/  – quer zu allen Schichten: JWT, Rollen, BCrypt, CORS
```

| Package | Inhalt |
|---|---|
| `controller` | 7 REST-Controller, einheitliche Fehlerbehandlung über `@ExceptionHandler` |
| `service` | `ProcessService`, `ProductConfigurationService`, `UserService`, `StatisticsService`, `CategoryService`, `ValidationService`, `LoggingService` |
| `repository` | Spring-Data-JPA-Repositories |
| `domain` | JPA-Entities: `Process`, `PackageUnit`, `Portion`, `ProductConfiguration`, `Category`, `User`, `UserSession`, `AuditLog` + Übersetzungstabellen |
| `dto` | Request-/Response-DTOs – Entities verlassen die Service-Schicht nicht |
| `security` | `SecurityConfig`, `AuthService`, `TokenService`, `JwtConfig`, `PasswordService`, `AuthorizationService`, `CorsConfig` |
| `simulation` | `WeighingCore`, `SimulationManager`, `CombinationAlgorithm`, `InputSimulator`, `RuntimeSnapshot`, Bucket-Klassen |
| `config` | `DataInitializer` – legt Startdaten an |

Fehlerbehandlung: `IllegalArgumentException` (z. B. aus dem `ValidationService`)
wird als **400 Bad Request** beantwortet, unerwartete `RuntimeException` als
**500 Internal Server Error**. Aktionen ohne Rückgabedaten liefern **204 No
Content**.

---

## Die Simulation

### Kanalstruktur

Simuliert werden **8 Kanäle** (`WeighingCore.CHANNEL_COUNT`). Jeder Kanal besteht
aus einem `BufferBucket`, einem `WeighingBucket` und **zwei** `MemoryBucket`s
(`MEMORY_BUCKETS_PER_CHANNEL`) – analog zu den Memory Pans der realen Anlage.

```
                    InputSimulator
                          │
   ┌──────────┬───────────┼───────────┬──────────┐
   ▼          ▼           ▼           ▼          ▼
 Kanal 1    Kanal 2     Kanal 3     ...       Kanal 8
   │          │           │                     │
BufferBucket ─┴─ … ───────┴─────────────────────┘
   ▼
WeighingBucket
   ▼
Memory A   Memory B
```

Alle Bucket-Typen implementieren ein gemeinsames `Bucket`-Interface, damit der
`WeighingCore` sie einheitlich befüllen, leeren und prüfen kann.

### Ablauf eines Simulationsticks

Der `SimulationManager` ruft über einen `ScheduledExecutorService` alle
**500 ms** (`DEFAULT_TICK_INTERVAL_MS`) einen Tick im `WeighingCore` auf. Jeder
Tick durchläuft drei Phasen:

1. **Materialfluss** – nach dem *Pull-Prinzip*: eine Portion rückt nur vor, wenn
   die nächste Stufe frei ist. Reihenfolge: Weighing → Memory, Buffer → Weighing,
   InputSimulator → Buffer.
2. **Kombinationsermittlung** – der `CombinationAlgorithm` durchsucht alle
   belegten MemoryBuckets **kanalübergreifend** nach einer Kombination, deren
   Summe innerhalb der Toleranz um das Zielgewicht liegt:

   ```
   Abweichung = |Zielgewicht − Summe der Kombination|
   ```

   Gewählt wird die Kombination mit der geringsten Abweichung; bei Gleichstand
   jene mit weniger Portionen. Danach erzeugt der `WeighingCore` über
   `createPackage()` eine neue Verpackungseinheit und gibt die beteiligten
   Buckets frei.
3. **Rückführung** – findet eine Portion keine Kombination, steigt der
   Iterationszähler ihres MemoryBuckets. Überschreitet er den Parameter
   *Max. Versuche*, wird die Portion als **Deadlock** aus dem Bucket entfernt und
   ohne Paketzuordnung gespeichert. So blockiert keine Portion dauerhaft einen
   Speicherplatz.

Der Prozess endet automatisch, sobald die konfigurierte Ziel-Menge erreicht ist,
oder manuell über `POST /api/process/{id}/stop`.

### Laufzeitüberwachung

Nach jedem Tick aktualisiert der `WeighingCore` einen `RuntimeSnapshot` mit
Status, produzierten Einheiten, erkannten Deadlocks, zuletzt verarbeiteter
Portion und dem Zustand jedes einzelnen Buckets. Das Frontend liest ihn über
`GET /api/process/{id}/status`.

Ist `DEBUG_CONSOLE_OUTPUT` im `SimulationManager` aktiv (aktuell `true`), wird
derselbe Snapshot zusätzlich pro Tick auf die Konsole geschrieben:

```
Tick: 43 | Status: RUNNING | Packages: 38 | Deadlocks: 16
Message: Package 38 erstellt (250g)
------------------------------------------------------------
Channel Buffer    Weighing  Memory A   Memory B
1       98g       122g      -          105g
2       77g       84g       67g        96g
...
```

---

## REST-API

Basis-Pfad: `/api`. Die Rechte stammen aus `SecurityConfig`.

| Methode | Endpunkt | Erlaubt für | Zweck |
|---|---|---|---|
| POST | `/auth/login` | alle | Anmeldung, liefert JWT |
| POST | `/auth/logout` | angemeldet | Session invalidieren |
| GET | `/ping` | alle | Health-Check des Frontends |
| GET | `/process` | angemeldet | Prozessliste (Admin: alle, sonst eigene) |
| GET | `/process/{id}` | angemeldet¹ | Prozessdetails |
| POST | `/process/start` | admin, operator | Prozess starten |
| POST | `/process/{id}/stop` | admin, operator¹ | Prozess stoppen |
| GET | `/process/{id}/status` | angemeldet | Live-Status (RuntimeSnapshot) |
| GET | `/process/active` | angemeldet | Läuft gerade ein Prozess? (200 / 204) |
| GET | `/products` | angemeldet | Produktkatalog |
| POST | `/products` | admin | Produkt anlegen |
| PUT | `/products/{id}` | admin | Produkt ändern (Partial Update) |
| DELETE | `/products/{id}` | admin | Produkt de-/reaktivieren (**Soft Delete**) |
| GET | `/category`, `/category/by` | angemeldet | Kategorien (auch gefiltert) |
| POST | `/category` | angemeldet | Kategorie anlegen |
| GET | `/statistics` | angemeldet | Gesamtkennzahlen |
| GET | `/statistics/{processId}` | angemeldet | Kennzahlen je Prozess |
| GET | `/statistics/product/{productId}` | angemeldet | Kennzahlen je Produkt |
| GET | `/users` | admin | Alle Benutzer |
| POST | `/users` | admin | Benutzer anlegen |
| GET | `/users/{userId}` | angemeldet¹ | Benutzerprofil |
| PUT | `/users/{userId}` | angemeldet¹ | Profil ändern |
| DELETE | `/users/{userId}` | admin | Benutzer de-/aktivieren |

¹ Zusätzlich ressourcenbezogen geprüft: Nicht-Admins erhalten **403 Forbidden**,
wenn die Ressource nicht ihnen gehört. Diese Prüfung erfolgt im Controller bzw.
im `AuthorizationService`, weil die endpunktbasierten Regeln der `SecurityConfig`
dafür nicht ausreichen.

`DELETE /products/{id}` löscht nicht physisch, sondern schaltet aktiv/inaktiv –
die Produktkonfiguration bleibt für Statistiken historisch nachvollziehbar.

---

## Security

| Baustein | Umsetzung |
|---|---|
| Authentifizierung | `POST /api/auth/login` → JWT (HS256), Gültigkeit 120 Minuten |
| Tokenprüfung | Spring Security OAuth2 Resource Server, Bearer Token im `Authorization`-Header |
| Rollen | `admin`, `operator`, `viewer` – im JWT-Claim `role`, gemappt auf `ROLE_*` |
| Passwörter | BCrypt-Hash, nie im Klartext gespeichert (`PasswordService`) |
| Sessions | Serverseitig `STATELESS`; `UserSession`-Einträge werden bei Deaktivierung eines Benutzers invalidiert |
| CORS | `CorsConfig`, erlaubt standardmässig `http://localhost:5173` |

Wird ein Benutzer deaktiviert, verliert er sofort den Zugriff – auch mit einem
noch gültigen Token.

---

## Mehrsprachigkeit

Produktnamen, Beschreibungen und Kategorien liegen in **Deutsch, Englisch und
Französisch** vor. Umgesetzt ist das über relationale Übersetzungstabellen
(`ProductConfigurationTranslation`, `CategoryTranslation`) statt über Spalten pro
Sprache. Auch die Statusmeldungen des Prozesses liefert das Backend in der
Sprache des angemeldeten Benutzers.

---

## Starten

### Entwicklung (H2 In-Memory)

```bash
./mvnw spring-boot:run
```

Läuft auf `http://localhost:8080`. Die Datenbank wird bei jedem Start neu
aufgebaut (`ddl-auto=create-drop`) und über den `DataInitializer` mit
Testbenutzern und einem Produktkatalog befüllt.

H2-Konsole: `http://localhost:8080/h2-console` (JDBC-URL `jdbc:h2:mem:packopsdb`,
Benutzer `sa`, kein Passwort).

### Produktion (Docker + PostgreSQL)

Voraussetzung: `packops-backend` und `packops-frontend` liegen im selben
übergeordneten Verzeichnis, die Ports 80, 8080 und 5432 sind frei.

```bash
docker-compose up -d --build
```

Startet Backend, Frontend (Nginx) und PostgreSQL. Das Frontend ist danach unter
`http://localhost/` erreichbar. Im Docker-Profil legt der `DataInitializer`
**nur** den Admin-Account an – keine Testbenutzer, keine Dummy-Produkte.

Weitere Befehle:

```bash
docker-compose logs -f
```

```bash
docker-compose down
```

`docker-compose down -v` löscht zusätzlich das Volume und damit die Datenbank
unwiderruflich.

### Demo-Zugänge (nur Entwicklungsumgebung)

| Benutzer | Passwort | Rolle |
|---|---|---|
| `admin` | `admin123` | admin |
| `operator` | `operator123` | operator |
| `viewer` | `viewer123` | viewer |

Reine Demo-Zugänge einer lokal laufenden Übungsanwendung – für einen echten
Betrieb zwingend zu ersetzen.

---

## Konfiguration

| Property | Standard | Bemerkung |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:mem:packopsdb` | im Docker-Profil über `SPRING_DATASOURCE_URL` auf PostgreSQL umgebogen |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Docker: `update` |
| `app.security.jwt.secret` | Entwicklungs-Secret | siehe Einschränkungen |
| `app.security.jwt.issuer` | `packops-backend` | |
| `app.security.jwt.expiration-minutes` | `120` | |
| `app.cors.allowed-origins` | `http://localhost:5173` | Vite-Dev-Server |

Simulationsparameter stehen als Konstanten im Code: `CHANNEL_COUNT = 8`,
`MEMORY_BUCKETS_PER_CHANNEL = 2`, `DEFAULT_TICK_INTERVAL_MS = 500`,
`DEBUG_CONSOLE_OUTPUT = true`.

---

## Tests

```bash
./mvnw test
```

| Ebene | Tests |
|---|---|
| Unit | `ValidationServiceTest`, `ProductConfigurationServiceTest`, `UserServiceTest`, `LoggingServiceTest`, `ProcessServiceTest`, `StatisticsServiceTest` |
| Integration | `ProcessControllerIntegrationTest`, `ProductConfigurationControllerIntegrationTest`, `UserControllerIntegrationTest`, `StatisticsControllerIntegrationTest` |
| Security | `SecurityIntegrationTest` |
| Simulation | `WeighingCoreConsoleTest` |

End-to-End-Tests (Playwright, 8 Testfälle) liegen im Frontend-Repository.

---

## Bekannte Einschränkungen

- **JWT-Secret im Repository.** `app.security.jwt.secret` steht im Klartext in
  `application.properties`. Für den Schulbetrieb bewusst so gewählt; produktiv
  gehört es in eine Umgebungsvariable.
- **H2-Konsole offen.** `/h2-console/**` ist `permitAll` und `frameOptions` sind
  deaktiviert. Praktisch für die Entwicklung, ausserhalb davon abzuschalten.
- **CSRF deaktiviert.** Vertretbar bei einer zustandslosen API mit
  Bearer-Token-Authentifizierung, aber bewusst zu entscheiden.
- **Regeln für `/api/configuration` ohne Controller.** Die `SecurityConfig`
  enthält Regeln für `/api/configuration`; einen zugehörigen Controller gibt es
  nach dem Aufräumen der globalen Maschinenkonfiguration nicht mehr. Die Regeln
  laufen ins Leere.
- **Kein Schema-Management.** Die Datenbank entsteht über `ddl-auto`; es gibt
  keine Migrationen (Flyway/Liquibase).
- **Debug-Ausgabe standardmässig an.** `DEBUG_CONSOLE_OUTPUT = true` schreibt bei
  jedem Tick eine Tabelle auf die Konsole.
- **Ein Prozess zur Zeit.** Die Simulation ist auf einen laufenden Prozess pro
  Instanz ausgelegt (`GET /api/process/active`).

---

## Code-Verantwortlichkeiten

Gemäss Projektdokumentation, Tabelle 6:

| Bereich | Verantwortlich |
|---|---|
| Prozess / Simulation – Übersicht, Details, Statusabfrage | Kapischan Sriganthan |
| Prozess / Simulation – Kernfunktionalität, Kombinationslogik | David Thomi |
| ProductConfiguration, User, Validation, Logging | Kapischan Sriganthan |
| Security (Auth, Token, Rollen) | Mladen Radovanovic |
| Statistik | David Mitkov |
| Ping / Health-Check | Teodor Glisic |

Team: Teodor Glisic, David Mitkov, Mladen Radovanovic, Kapischan Sriganthan,
David Thomi · Dozent: Lukas Frey
