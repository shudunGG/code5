package main

import (
	"context"
	"errors"
	"log/slog"
	"time"

	"stablecoin-audit/pkg/config"
)

// Indexer wires together block subscriptions, decoding and sinks.
type Indexer struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
}

func NewIndexer(cfg config.ServiceConfig, logger *slog.Logger) *Indexer {
	return &Indexer{cfg: cfg, logger: logger}
}

func (i *Indexer) Run(ctx context.Context) error {
	i.logger.Info("starting indexer", slog.Any("brokers", i.cfg.KafkaBrokers))
	ticker := time.NewTicker(15 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			i.logger.Info("context cancelled, shutting down")
			return ctx.Err()
		case <-ticker.C:
			i.logger.Debug("heartbeat", slog.String("component", "indexer"))
		}
	}
}

// ProcessBlock is intentionally left with pseudo-code to guide implementation.
func (i *Indexer) ProcessBlock(ctx context.Context, blockNumber uint64) error {
	if blockNumber == 0 {
		return errors.New("block number cannot be zero")
	}
	// 1. Fetch block via RPC.
	// 2. Decode transaction logs with registered ABI catalog.
	// 3. Push events to ClickHouse and Kafka using shared sinks.
	// 4. Track reorg safety using confirmation depth.
	return nil
}
