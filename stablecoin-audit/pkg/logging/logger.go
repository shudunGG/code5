package logging

import (
	"log/slog"
	"os"
)

// NewLogger configures a structured slog Logger with level selection via LOG_LEVEL env.
func NewLogger() *slog.Logger {
	level := new(slog.LevelVar)
	switch os.Getenv("LOG_LEVEL") {
	case "debug":
		level.Set(slog.LevelDebug)
	case "warn":
		level.Set(slog.LevelWarn)
	case "error":
		level.Set(slog.LevelError)
	default:
		level.Set(slog.LevelInfo)
	}
	return slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{Level: level}))
}
