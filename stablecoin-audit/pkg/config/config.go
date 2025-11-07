package config

import (
	"fmt"
	"os"
	"strconv"
	"strings"
)

// ServiceConfig captures shared infrastructure configuration that is reused across services.
type ServiceConfig struct {
	PostgresURL    string
	ClickHouseURL  string
	RedisURL       string
	KafkaBrokers   []string
	MinIOEndpoint  string
	MinIOAccessKey string
	MinIOSecretKey string
	Environment    string
}

// LoadFromEnv hydrates ServiceConfig from environment variables with basic validation.
func LoadFromEnv() (ServiceConfig, error) {
	cfg := ServiceConfig{
		PostgresURL:    os.Getenv("DB_URL_PG"),
		ClickHouseURL:  os.Getenv("DB_URL_CH"),
		RedisURL:       os.Getenv("REDIS_URL"),
		KafkaBrokers:   splitAndTrim(os.Getenv("KAFKA_BROKERS")),
		MinIOEndpoint:  os.Getenv("MINIO_ENDPOINT"),
		MinIOAccessKey: os.Getenv("MINIO_ACCESS_KEY"),
		MinIOSecretKey: os.Getenv("MINIO_SECRET_KEY"),
		Environment:    defaultString(os.Getenv("ENVIRONMENT"), "dev"),
	}

	if cfg.PostgresURL == "" {
		return cfg, fmt.Errorf("DB_URL_PG must be provided")
	}
	if cfg.ClickHouseURL == "" {
		return cfg, fmt.Errorf("DB_URL_CH must be provided")
	}
	if len(cfg.KafkaBrokers) == 0 {
		return cfg, fmt.Errorf("KAFKA_BROKERS must contain at least one broker")
	}
	return cfg, nil
}

// Helper used by services that expose port configuration.
func ReadPortEnv(key string, fallback int) (int, error) {
	val := strings.TrimSpace(os.Getenv(key))
	if val == "" {
		return fallback, nil
	}
	port, err := strconv.Atoi(val)
	if err != nil {
		return 0, fmt.Errorf("invalid port for %s: %w", key, err)
	}
	return port, nil
}

func splitAndTrim(in string) []string {
	if in == "" {
		return nil
	}
	parts := strings.Split(in, ",")
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		if trimmed := strings.TrimSpace(p); trimmed != "" {
			out = append(out, trimmed)
		}
	}
	return out
}

func defaultString(in, fallback string) string {
	if strings.TrimSpace(in) == "" {
		return fallback
	}
	return in
}
