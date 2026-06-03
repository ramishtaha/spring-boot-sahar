# Deploying Sahar to Google Cloud Run (optional)

Cloud Run runs a single container for you, gives it a public HTTPS URL, and **scales to zero** when
no one is using it (so an idle personal app costs ~nothing). It is the smallest possible "real deploy"
and a good fit for Sahar.

> You need the `gcloud` CLI installed and a Google Cloud project with billing enabled.
> Docs: https://cloud.google.com/run/docs/quickstarts/deploy-container

## 1. One-time setup

```bash
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
gcloud services enable run.googleapis.com cloudbuild.googleapis.com
```

## 2. Deploy straight from source

Cloud Run can build the image for you from this folder's `Dockerfile` using Cloud Build:

```bash
gcloud run deploy sahar \
  --source . \
  --region asia-south1 \
  --allow-unauthenticated \
  --port 8080
```

The command prints a URL like `https://sahar-xxxxx-uc.a.run.app`. Open it - that is your routine,
live. Cloud Run sets the `PORT` env var; the app already reads it (`server.port=${PORT:8080}`).

With no database configured it uses the in-container H2 file, which is **ephemeral** on Cloud Run -
fine for a quick public demo, but data resets when the instance is recycled.

## 3. Persisting data (managed Postgres)

For data that survives, point the app at a managed Postgres (e.g. Cloud SQL) using the same env vars
the `postgres` profile reads:

```bash
gcloud run deploy sahar \
  --source . \
  --region asia-south1 \
  --allow-unauthenticated \
  --set-env-vars "SPRING_PROFILES_ACTIVE=postgres,SAHAR_DB_URL=jdbc:postgresql://HOST:5432/sahar,SAHAR_DB_USERNAME=sahar,SAHAR_DB_PASSWORD=..." \
  --add-cloudsql-instances YOUR_PROJECT:REGION:INSTANCE
```

(Connecting Cloud Run to Cloud SQL has a couple of extra wrinkles - the Cloud SQL socket factory or the
proxy. That is beyond this codealong; the point is that **nothing in the app changes** - only env vars.)

## Or: stop at local

You do not have to deploy to finish the course. `docker compose up` on your own machine is a complete,
honest "it runs in production-shaped infrastructure" result. The deploy is a victory lap, not a gate.
