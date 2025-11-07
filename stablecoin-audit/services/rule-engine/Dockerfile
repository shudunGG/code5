FROM golang:1.21 AS build
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . ./
RUN CGO_ENABLED=0 go build -o /bin/rule-engine ./services/rule-engine

FROM gcr.io/distroless/base-debian12
COPY --from=build /bin/rule-engine /bin/rule-engine
COPY rule-dsl /app/rule-dsl
ENTRYPOINT ["/bin/rule-engine"]
