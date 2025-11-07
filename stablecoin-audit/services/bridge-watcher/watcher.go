package main

import (
	"context"
	"log/slog"
	"time"

	"stablecoin-audit/pkg/config"
)

// Watcher inspects lock/mint/burn/unlock relations between chains.
type Watcher struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
}

func NewWatcher(cfg config.ServiceConfig, logger *slog.Logger) *Watcher {
	return &Watcher{cfg: cfg, logger: logger}
}

func (w *Watcher) Run(ctx context.Context) error {
	ticker := time.NewTicker(5 * time.Minute)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-ticker.C:
			w.logger.Info("bridge diff summary", slog.Int("pending_pairs", len(w.findMismatches())))
		}
	}
}

func (w *Watcher) findMismatches() []string {
	// TODO: query ClickHouse materialized views and return unmatched flows.
	return []string{}
}
