# User Management API

Applicazione backend REST sviluppata come esercizio tecnico per la gestione dell'entita `User`.

Il progetto e' pensato per essere semplice da leggere, rapido da avviare e pronto per essere pubblicato su una repository GitHub pubblica.

## Stack tecnologico

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Jakarta Validation
- PostgreSQL
- Maven
- springdoc OpenAPI / Swagger UI
- Docker e Docker Compose

## Funzionalita implementate

- CRUD completo per l'entita `User`
- ricerca utenti con filtri opzionali `nome` e `cognome`
- import utenti da file CSV con upload multipart
- validazione input su payload REST e righe CSV
- email univoca
- gestione centralizzata degli errori
- documentazione API via Swagger UI

## Struttura del progetto

```text
src/main/java/com/colloquio/usermanagement
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
└── service
```

## Requisiti

Per avvio locale:

- Java 21
- Maven 3.9+
- PostgreSQL 16+ oppure Docker per eseguire il database

Per avvio containerizzato:

- Docker
- Docker Compose

## Avvio con Docker

1. Costruire e avviare i container:

```bash
docker compose up --build
```

2. Applicazione disponibile su:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

3. Arrestare i container:

```bash
docker compose down
```

Per rimuovere anche il volume del database:

```bash
docker compose down -v
```

## Avvio locale

1. Avviare PostgreSQL locale oppure il solo database via Docker:

```bash
docker compose up db
```

2. Verificare o impostare le variabili ambiente se necessario:

```bash
DB_URL=jdbc:postgresql://localhost:5432/user_management
DB_USERNAME=postgres
DB_PASSWORD=postgres
SERVER_PORT=8080
```

3. Avviare l'applicazione:

```bash
./mvnw spring-boot:run
```

Su Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Endpoint disponibili

| Metodo | Endpoint | Descrizione |
| --- | --- | --- |
| POST | `/api/users` | Crea un utente |
| GET | `/api/users/{id}` | Recupera un utente per id |
| GET | `/api/users` | Elenca tutti gli utenti |
| PUT | `/api/users/{id}` | Aggiorna un utente |
| DELETE | `/api/users/{id}` | Elimina un utente |
| GET | `/api/users/search?nome=&cognome=` | Ricerca utenti con filtri opzionali |
| POST | `/api/users/import-csv` | Import utenti da file CSV |

## Esempi di chiamate API

### Creazione utente

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Mario",
    "cognome": "Rossi",
    "email": "mario.rossi@example.com",
    "indirizzo": "Via Roma 1, Milano"
  }'
```

### Lettura di un utente

```bash
curl http://localhost:8080/api/users/1
```

### Elenco utenti

```bash
curl http://localhost:8080/api/users
```

### Ricerca per nome

```bash
curl "http://localhost:8080/api/users/search?nome=Mario"
```

### Ricerca per cognome

```bash
curl "http://localhost:8080/api/users/search?cognome=Rossi"
```

### Ricerca per nome e cognome

```bash
curl "http://localhost:8080/api/users/search?nome=Mario&cognome=Rossi"
```

### Aggiornamento utente

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Mario",
    "cognome": "Rossi",
    "email": "mario.rossi@example.com",
    "indirizzo": "Via Torino 20, Milano"
  }'
```

### Eliminazione utente

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

### Import CSV

```bash
curl -X POST http://localhost:8080/api/users/import-csv \
  -F "file=@example-users.csv"
```

## Formato CSV atteso

Il file deve contenere una riga di intestazione con queste colonne:

```csv
nome,cognome,email,indirizzo
```

Comportamento dell'import:

- il file viene accettato come `multipart/form-data`
- le righe valide vengono salvate nel database
- le righe non valide o duplicate vengono scartate
- la risposta restituisce il totale importato, il totale scartato e il dettaglio delle righe rifiutate

Esempio di risposta:

```json
{
  "importedCount": 2,
  "discardedCount": 1,
  "discardedRows": [
    {
      "rowNumber": 4,
      "message": "Email duplicata: mario.rossi@example.com"
    }
  ]
}
```

## Gestione errori

Le API restituiscono errori JSON uniformi, ad esempio:

```json
{
  "timestamp": "2026-03-26T14:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Richiesta non valida",
  "path": "/api/users",
  "fieldErrors": [
    {
      "field": "email",
      "message": "L'email non e' valida"
    }
  ]
}
```

## Build del progetto

Compilazione:

```bash
./mvnw clean package
```

Su Windows:

```powershell
.\mvnw.cmd clean package
```

## Note progettuali

- nessun layer superfluo o autenticazione non richiesta
- DTO separati dall'entita JPA
- mapping dedicato in una classe semplice
- ricerca implementata con filtri opzionali tramite `JpaSpecificationExecutor`
- persistenza gestita su PostgreSQL

## File utili inclusi

- `Dockerfile`
- `docker-compose.yml`
- `.gitignore`
- `example-users.csv`
- Swagger UI integrata
