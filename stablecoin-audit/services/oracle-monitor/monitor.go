package main

import (
	"context"
	"log/slog"
	"math"
	"time"

	"stablecoin-audit/pkg/config"
)

// Monitor compares oracle feeds to detect divergence.
type Monitor struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
}

func NewMonitor(cfg config.ServiceConfig, logger *slog.Logger) *Monitor {
	return &Monitor{cfg: cfg, logger: logger}
}

func (m *Monitor) Run(ctx context.Context) error {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-ticker.C:
			feeds := []float64{1.0, 1.001, 0.999}
			z := zScore(feeds)
			m.logger.Debug("oracle zscore", slog.Float64("z", z))
		}
	}
}

func zScore(samples []float64) float64 {
	n := float64(len(samples))
	if n == 0 {
		return 0
	}
	mean := 0.0
	for _, v := range samples {
		mean += v
	}
	mean /= n
	variance := 0.0
	for _, v := range samples {
		variance += math.Pow(v-mean, 2)
	}
	if variance == 0 {
		return 0
	}
	stdDev := math.Sqrt(variance / n)
	return (samples[0] - mean) / stdDev
}
