package main

import (
	"context"
	"log/slog"
	"os/signal"
	"syscall"

	"stablecoin-audit/pkg/config"
	"stablecoin-audit/pkg/logging"
)

func main() {
	logger := logging.NewLogger()
	cfg, err := config.LoadFromEnv()
	if err != nil {
		logger.Error("config error", slog.Any("err", err))
		return
	}

	re, err := NewEngine(cfg, logger)
	if err != nil {
		logger.Error("bootstrap rule engine", slog.Any("err", err))
		return
	}
	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	if err := re.Run(ctx); err != nil {
		logger.Error("rule engine stopped", slog.Any("err", err))
	}
}
