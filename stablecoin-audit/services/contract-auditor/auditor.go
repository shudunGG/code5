package main

import (
	"context"
	"log/slog"
	"time"

	"stablecoin-audit/pkg/config"
)

// Auditor coordinates smart contract scanning jobs.
type Auditor struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
}

func NewAuditor(cfg config.ServiceConfig, logger *slog.Logger) *Auditor {
	return &Auditor{cfg: cfg, logger: logger}
}

func (a *Auditor) Run(ctx context.Context) error {
	a.logger.Info("contract auditor online")
	ticker := time.NewTicker(1 * time.Minute)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			a.logger.Info("shutdown signal received")
			return ctx.Err()
		case <-ticker.C:
			a.logger.Debug("scheduled baseline scan tick")
		}
	}
}

// ScanBaseline contains the scoring blueprint from the design document.
type ScanBaselineResult struct {
	OwnerType      string
	RiskScore      uint32
	Findings       []Finding
	TokenAddress   string
	Implementation string
	ProxyAddress   string
}

type Finding struct {
	Code     string
	Title    string
	Severity string
	Detail   string
}

func (a *Auditor) ScanBaseline(ctx context.Context, tokenAddress string) (ScanBaselineResult, error) {
	// TODO: integrate with on-chain RPC and ABI inspection
	result := ScanBaselineResult{OwnerType: "EOA", TokenAddress: tokenAddress, RiskScore: 40}
	result.Findings = append(result.Findings, Finding{
		Code:     "OWNER_EOA",
		Title:    "Owner is externally owned account",
		Severity: "HIGH",
		Detail:   "Single key risk detected, consider multisig or timelock",
	})
	return result, nil
}
