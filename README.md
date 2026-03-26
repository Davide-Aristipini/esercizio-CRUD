# User Management API

Al primo avvio l'applicazione verifica la presenza della tabella `users` sul database. Se la tabella e' disponibile e il database risulta ancora vuoto, puo caricare automaticamente alcuni dati mock utili per fare prove veloci.

Questo comportamento e' controllato da questa property nel file [application.yml](/src/main/resources/application.yml):

```yml
app:
  mock-data:
    enabled: true
```


L'applicazione permette di:
- creare, leggere, aggiornare ed eliminare utenti
- cercare utenti per `nome` e/o `cognome`
- importare utenti da file CSV
- salvare i dati su PostgreSQL

Lo stack utilizzato e' composto da Java 21, Spring Boot, Spring Data JPA, PostgreSQL, Maven, Jakarta Validation, Docker e Swagger UI.

## Modello dati

Ogni utente ha i seguenti campi:

- `id`
- `nome`
- `cognome`
- `email`
- `indirizzo`

L'email e' univoca e viene validata insieme agli altri campi obbligatori.

## Avvio rapido con Docker

Per usare il progetto avviarlo con Docker Compose dalla root della repository:

```bash
docker compose up --build
```

Al termine dell'avvio:

- applicazione: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`


## Import CSV

Nel repository e' presente il file [example-users.csv](/example-users.csv), utile per testare l'import.

Il CSV deve avere questa intestazione:

```csv
nome,cognome,email,indirizzo
```

L'import puo essere eseguito direttamente da Swagger UI aprendo l'endpoint `POST /api/users/import-csv` e caricando il file dal form multipart.

La risposta restituisce:

- numero di utenti importati
- numero di utenti scartati
- dettaglio delle eventuali righe non valide o duplicate
