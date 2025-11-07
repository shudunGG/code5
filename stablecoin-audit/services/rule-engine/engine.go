package main

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"time"

	"stablecoin-audit/pkg/config"
	"stablecoin-audit/pkg/kafka"
	"stablecoin-audit/pkg/metrics"
	"stablecoin-audit/pkg/rules"
)

// Engine evaluates DSL rules by fetching metrics and emitting events.
type Engine struct {
	cfg      config.ServiceConfig
	logger   *slog.Logger
	ruleSet  []rules.Rule
	provider metrics.Provider
}

func NewEngine(cfg config.ServiceConfig, logger *slog.Logger) (*Engine, error) {
	ruleLoader := rules.NewLoader("rule-dsl/rules")
	rules, err := ruleLoader.Load()
	if err != nil {
		return nil, fmt.Errorf("load rules: %w", err)
	}
	return &Engine{cfg: cfg, logger: logger, ruleSet: rules, provider: metrics.NoopProvider{}}, nil
}

func (e *Engine) Run(ctx context.Context) error {
	e.logger.Info("rule engine running", slog.Int("rules", len(e.ruleSet)))
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			e.logger.Info("context done")
			return ctx.Err()
		case <-ticker.C:
			for _, rule := range e.ruleSet {
				if err := e.evaluate(ctx, rule); err != nil {
					e.logger.Warn("rule evaluation failed", slog.String("rule", rule.ID), slog.Any("err", err))
				}
			}
		}
	}
}

func (e *Engine) evaluate(ctx context.Context, rule rules.Rule) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	default:
	}
	if len(rule.Clauses) == 0 {
		return errors.New("rule missing clauses")
	}
	clause := rule.Clauses[0]
	sample, err := e.provider.Fetch(0, clause.Metric, clause.Window)
	if err != nil {
		return err
	}
	if compare(clause.Op, sample.Value, clause.Value) {
		e.emit(rule, sample)
	}
	return nil
}

func (e *Engine) emit(rule rules.Rule, sample metrics.Sample) {
	env := kafka.NewEnvelope("rule-engine", "ALERT", sample.ProjectID, map[string]any{
		"rule_id": rule.ID,
		"metric":  sample.Name,
		"value":   sample.Value,
	})
	e.logger.Info("alert", slog.Any("envelope", env))
}

func compare(op string, lhs, rhs float64) bool {
	switch op {
	case ">":
		return lhs > rhs
	case ">=":
		return lhs >= rhs
	case "<":
		return lhs < rhs
	case "<=":
		return lhs <= rhs
	case "==":
		return lhs == rhs
	case "!=":
		return lhs != rhs
	default:
		return false
	}
}

// Noop provider lives here to avoid cyclic dependency.
var _ metrics.Provider = metrics.NoopProvider{}
