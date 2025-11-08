package main

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"os/signal"
	"syscall"

	stdhttp "stablecoin-audit/pkg/http"

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

	svc := NewService(cfg, logger)
	handler := http.NewServeMux()
	handler.HandleFunc("/v1/compliance/score", svc.handleScore)

	port, err := config.ReadPortEnv("HTTP_PORT", 8085)
	if err != nil {
		logger.Error("invalid port", slog.Any("err", err))
		return
	}
	server := stdhttp.New(fmt.Sprintf(":%d", port), handler)

	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	if err := server.Run(ctx); err != nil {
		logger.Error("compliance server stopped", slog.Any("err", err))
	}
}
