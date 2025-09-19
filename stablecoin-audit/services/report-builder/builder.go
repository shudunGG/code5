package main

import (
	"context"
	"log/slog"
	"time"

	"stablecoin-audit/pkg/config"
)

// Builder aggregates findings and templates into final reports.
type Builder struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
}

func NewBuilder(cfg config.ServiceConfig, logger *slog.Logger) *Builder {
	return &Builder{cfg: cfg, logger: logger}
}

func (b *Builder) Run(ctx context.Context) error {
	ticker := time.NewTicker(1 * time.Hour)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-ticker.C:
			report := b.BuildReport("example-project")
			b.logger.Info("report compiled", slog.String("project", report.ProjectName))
		}
	}
}

type Report struct {
	ProjectName string
	GeneratedAt time.Time
	Findings    []string
}

func (b *Builder) BuildReport(project string) Report {
	return Report{
		ProjectName: project,
		GeneratedAt: time.Now().UTC(),
		Findings: []string{
			"Owner is multisig",
			"Reserve ratio healthy",
		},
	}
}
