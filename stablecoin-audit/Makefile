.PHONY: tidy test lint proto docker-build

tidy:
go mod tidy

lint:
@echo "golangci-lint and mypy hooks can be added here"

test:
go test ./...

proto:
@echo "Run protoc to generate Go/Python stubs"

docker-build:
docker build -t stablecoin-audit/rule-engine -f services/rule-engine/Dockerfile .
