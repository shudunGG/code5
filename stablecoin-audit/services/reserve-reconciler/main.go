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
	reconciler := NewReconciler(cfg, logger)
	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	if err := reconciler.Run(ctx); err != nil {
		logger.Error("reconciler stopped", slog.Any("err", err))
	}
}
