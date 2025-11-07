package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"time"

	stdhttp "stablecoin-audit/pkg/http"

	"stablecoin-audit/pkg/config"
)

type Server struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
	http   stdhttp.Server
}

func NewServer(cfg config.ServiceConfig, logger *slog.Logger) Server {
	handler := http.NewServeMux()
	s := Server{cfg: cfg, logger: logger}
	handler.HandleFunc("/v1/projects", s.handleProjects)
	handler.HandleFunc("/v1/projects/", s.handleProjectDetail)
	handler.HandleFunc("/v1/alerts", s.handleAlerts)

	port, _ := config.ReadPortEnv("API_PORT", 8080)
	s.http = stdhttp.New(fmt.Sprintf(":%d", port), handler)
	return s
}

func (s Server) Run(ctx context.Context) error {
	s.logger.Info("api gateway listening")
	return s.http.Run(ctx)
}

type project struct {
	ID       uint64            `json:"id"`
	Name     string            `json:"name"`
	Symbol   string            `json:"symbol"`
	Chains   map[string]string `json:"chains"`
	Decimals uint32            `json:"decimals"`
}

type alert struct {
	ID       uint64      `json:"id"`
	Project  uint64      `json:"project_id"`
	Severity string      `json:"severity"`
	Title    string      `json:"title"`
	Details  interface{} `json:"details"`
}

func (s Server) handleProjects(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case http.MethodPost:
		var req project
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid payload", http.StatusBadRequest)
			return
		}
		req.ID = 1
		w.WriteHeader(http.StatusCreated)
		_ = json.NewEncoder(w).Encode(req)
	default:
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
	}
}

func (s Server) handleProjectDetail(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	resp := map[string]any{
		"baseline": map[string]any{
			"owner_type": "EOA",
			"risk_score": 70,
		},
	}
	_ = json.NewEncoder(w).Encode(resp)
}

func (s Server) handleAlerts(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	items := []alert{
		{ID: 1, Project: 1, Severity: "HIGH", Title: "Supply mismatch", Details: map[string]any{"delta": 0.03}},
	}
	_ = json.NewEncoder(w).Encode(map[string]any{
		"items":        items,
		"generated_at": time.Now().UTC(),
	})
}
