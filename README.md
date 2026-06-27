# AutoTranscription

A full-stack web application that records audio from the browser microphone and transcribes it into text using [Deepgram](https://deepgram.com/)'s speech-to-text API (Nova-3 model with smart formatting).

---

## Architecture

```
┌──────────────┐     POST /api/v1/transcribe      ┌──────────────┐     POST /v1/listen      ┌──────────┐
│  Frontend    │  ──────────────────────────▶   │   Backend    │  ──────────────────────▶  │ Deepgram │
│  React/TS    │  ◀──────────────────────────   │  Kotlin/Ktor │  ◀──────────────────────  │   API    │
│  Vite:5173   │      JSON {text, status}       │  Netty:8081  │    JSON transcription     └──────────┘
└──────────────┘                                └──────────────┘
       │
       ├── Records audio via MediaRecorder API
       ├── Sends WAV/WebM blob as multipart/form-data
       └── Displays transcript in review modal
```

### Backend (`backend/`)

| Layer | File | Responsibility |
|-------|------|----------------|
| Entry point | `Main.kt` | Ktor embedded Netty server, CORS, JSON content negotiation, routing, Swagger UI |
| Controller | `TranscriptionController.kt` | Handles `POST /api/v1/transcribe`, accepts multipart audio, delegates to service |
| Service | `TranscriptionService.kt` | Calls Deepgram API with retry logic (3 attempts, exponential backoff, rate-limit handling) |

- **Framework:** Ktor 2.3.6 (Netty engine), Ktor plugin 3.5.0
- **Language:** Kotlin 2.3.10
- **Build:** Gradle with JDK 25 toolchain
- **Serialization:** kotlinx-serialization-json 1.6.2
- **Configuration:** Deepgram API key via `DEEPGRAM_API_KEY` env var (loaded from `backend/.env`)
- **API docs:** OpenAPI 3.0 spec served at `/openapi.yaml`, Swagger UI at `/swagger-ui`

### Frontend (`frontend/`)

Organized using **Atomic Design** principles:

| Layer | Component | Purpose |
|-------|-----------|---------|
| Organism | `AudioRecorder` | Main recording UI — start/stop, transcribe, show results |
| Molecule | `TranscriptModal` | Review modal with Accept / Edit / Reject / Retry actions |
| Atom | `SoundWaveAnimation` | Animated bars during recording |
| Hook | `useMediaRecorder` | Wraps browser `MediaRecorder` API with MIME type negotiation |
| Service | `transcribeService` | Orchestrates API call and error handling |
| API | `transcribeApi` | Typed `fetch()` POST to `/api/v1/transcribe` via generated OpenAPI types |

- **Framework:** React 19.2.6 + TypeScript 6.0
- **Build:** Vite 8.0.12 (proxies `/api`, `/openapi.yaml`, `/swagger-ui` to `localhost:8081`)
- **Styling:** CSS Modules with SCSS (sass-embedded)
- **Testing:** Vitest 4
- **Linting:** ESLint 10 with typescript-eslint
- **API types:** Auto-generated from OpenAPI spec via openapi-typescript

---

## Features

- **Record audio** directly in the browser via the MediaRecorder API (supports WebM, Ogg, MP4, WAV)
- **Transcribe speech to text** using Deepgram Nova-3 (state-of-the-art model)
- **Smart formatting** — punctuation, capitalization, and formatting enabled
- **Review modal** — accept, edit (inline textarea), reject, or retry a transcript
- **Visual feedback** — sound wave animation during recording, spinner during transcription, error messages
- **Resilience** — automatic retry with exponential backoff (3 attempts), rate-limit (429) handling
- **API documentation** — built-in Swagger UI at `/swagger-ui`
- **Stateless** — no database; transcripts are ephemeral

---

## Getting Started

### Prerequisites

- JDK 25
- Node.js 20+
- A [Deepgram](https://deepgram.com/) API key

### Setup

```bash
# 1. Set your Deepgram API key
echo 'DEEPGRAM_API_KEY="your_key_here"' > backend/.env

# 2. Start the backend (port 8081)
cd backend
./gradlew run

# 3. Start the frontend (port 5173)
cd frontend
npm install
npm run dev
```

The frontend dev server proxies `/api/*` requests to the backend. Open http://localhost:5173 to use the app, or http://localhost:5173/swagger-ui for the API docs.

### Scripts

| Command | Description |
|---------|-------------|
| `./gradlew build` | Build the backend |
| `./gradlew run` | Run the backend |
| `./gradlew test` | Run backend tests |
| `npm install` | Install frontend dependencies |
| `npm run dev` | Start frontend dev server |
| `npm run build` | Build frontend for production |
| `npm test` | Run frontend tests |
| `npm run test:watch` | Run frontend tests in watch mode |
| `npm run lint` | Lint frontend source |
| `npm run generate-api-types` | Regenerate API types from OpenAPI spec |

---

## API

### `POST /api/v1/transcribe`

Accept an audio file and return the transcribed text.

**Request:** `multipart/form-data` with field `audio` (audio blob)

**Response:**
```json
{
  "text": "Transcribed text from Deepgram",
  "status": "success"
}
```

**Additional endpoints:**
- `GET /` — health check ("hello world")
- `GET /health` — returns `{"status":"ok"}`
- `GET /openapi.yaml` — OpenAPI 3.0 specification
- `GET /swagger-ui` — Swagger UI documentation browser

---

## Configuration

| Variable | Location | Purpose |
|----------|----------|---------|
| `DEEPGRAM_API_KEY` | `backend/.env` | Deepgram API authentication token |
| Vite proxy | `frontend/vite.config.ts` | Proxies `/api`, `/openapi.yaml`, `/swagger-ui` → `localhost:8081` |

---

## Testing

### Backend

```bash
cd backend
./gradlew test
```

The backend tests use Ktor's `testApplication` and `MockEngine` to test controller and service layers without real HTTP calls.

### Frontend

```bash
cd frontend
npm test
```

The frontend tests use Vitest with mocked API calls to verify the service and API layers.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend language | Kotlin 2.3.10 |
| Backend framework | Ktor 2.3.6 (Netty) |
| Frontend framework | React 19.2.6 |
| Frontend build | Vite 8.0.12 |
| Language (frontend) | TypeScript 6.0 |
| Styling | SCSS Modules (sass-embedded) |
| Transcription | Deepgram API (Nova-3) |
| API documentation | OpenAPI 3.0 / Swagger UI |

---

## Future Considerations

- **load balancer** - in case we have multiple backend services in order to guarantee high availability, we need a load balancer
- **authentication** — the API is currently open
- **database** — the application is stateless; recordings are temporary files deleted after transcription. In order to persists the data we need a database
- **encryption/masking** — production data should be encrypted, data on dev should be masked
- **logging/auditing** — add logging and auditing services since it's a hospital environment and we are dealing with patient data
- **multi tenancy** — if the product serves multiple hospitals and multiple nurses, we need data isolation

---
