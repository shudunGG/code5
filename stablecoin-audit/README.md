# Stablecoin Audit Platform

This repository implements the core scaffolding for a blockchain stablecoin security audit platform.  It follows the architecture described in the design brief and provides:

- Service skeletons for indexer, rule engine, contract auditor, reserve reconciler, oracle monitor, bridge watcher, compliance API, report builder, and API gateway.
- Shared Go packages for configuration, logging, Kafka envelope contracts, metrics interfaces, and HTTP server helpers.
- A Python anomaly detection worker example implementing a depeg index.
- Database migration files for PostgreSQL and ClickHouse.
- A configurable YAML-based rules DSL and loader implementation.
- Proto definitions for the public gRPC interface.
- Containerization and local development assets (Dockerfile, docker-compose).

The code is intentionally lightweight while capturing the major integration points so individual services can be fleshed out iteratively.

## Project Layout

```
services/            # Go microservices
pkg/                 # Shared Go utilities
proto/               # Protobuf contracts
migrations/          # Database schema migrations
rule-dsl/            # Rule DSL definitions and examples
services/algo-worker # Python analytics workers
web/                 # Front-end (placeholder)
deployments/         # Kubernetes/compose manifests
scripts/             # Developer tooling hooks
```

## Getting Started

1. Install Go 1.21+ and Python 3.11+.
2. Run `go mod tidy` to download dependencies.
3. Use `make up-dev` (to be implemented) or `docker compose -f deployments/docker-compose.yml up` to launch infrastructure.
4. Run individual services locally with the necessary environment variables, e.g.

```bash
export DB_URL_PG=postgres://postgres:postgres@localhost:5432/postgres?sslmode=disable
export DB_URL_CH=tcp://localhost:9000
export KAFKA_BROKERS=localhost:9092
export REDIS_URL=redis://localhost:6379
export MINIO_ENDPOINT=localhost:9001
export MINIO_ACCESS_KEY=minio
export MINIO_SECRET_KEY=minio123
export ENVIRONMENT=dev

go run ./services/rule-engine
```

## Testing

- Go unit tests can be added to each service to validate business logic (e.g., rule parsing, reconciliation math).
- Python analytics modules can be validated with `pytest` or `unittest`.

## Next Steps

- Implement actual blockchain integrations for the indexer and contract auditor.
- Hook rule engine outputs to Kafka and persistent alert stores.
- Expand metrics providers and analytics algorithms.
- Build the React front-end and CI/CD workflows.
