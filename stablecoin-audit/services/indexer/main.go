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
		logger.Error("load config", slog.Any("err", err))
		return
	}

	indexer := NewIndexer(cfg, logger)
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGTERM, syscall.SIGINT)
	defer stop()

	if err := indexer.Run(ctx); err != nil {
		logger.Error("indexer exited", slog.Any("err", err))
	}
}
