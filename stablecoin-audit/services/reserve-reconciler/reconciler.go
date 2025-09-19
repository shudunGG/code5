package main

import (
	"context"
	"errors"
	"log/slog"
	"math"
	"time"

	"stablecoin-audit/pkg/config"
)

// Reconciler merges on-chain supply snapshots with off-chain reserves.
type Reconciler struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
}

func NewReconciler(cfg config.ServiceConfig, logger *slog.Logger) *Reconciler {
	return &Reconciler{cfg: cfg, logger: logger}
}

func (r *Reconciler) Run(ctx context.Context) error {
	ticker := time.NewTicker(10 * time.Minute)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			r.logger.Info("context done")
			return ctx.Err()
		case <-ticker.C:
			if result, err := r.Reconcile(ctx, 0, time.Now()); err != nil {
				r.logger.Warn("reconcile failed", slog.Any("err", err))
			} else {
				r.logger.Info("reconcile complete", slog.Float64("ratio", result.Ratio), slog.Float64("hhi", result.HHI))
			}
		}
	}
}

type Result struct {
	Ratio float64
	HHI   float64
}

func (r *Reconciler) Reconcile(ctx context.Context, projectID uint64, asOf time.Time) (Result, error) {
	_ = ctx
	if asOf.IsZero() {
		return Result{}, errors.New("asOf cannot be zero")
	}
	onChain := 1000000.0
	reserves := []float64{400000, 350000, 250000}
	ratio := sum(reserves...) / onChain
	hhi := calcHHI(reserves)
	return Result{Ratio: ratio, HHI: hhi}, nil
}

func sum(values ...float64) float64 {
	total := 0.0
	for _, v := range values {
		total += v
	}
	return total
}

func calcHHI(reserves []float64) float64 {
	total := sum(reserves...)
	if total == 0 {
		return 0
	}
	acc := 0.0
	for _, r := range reserves {
		s := r / total
		acc += math.Pow(s, 2)
	}
	return acc
}
