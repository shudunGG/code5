package main

import (
	"encoding/json"
	"log/slog"
	"net/http"
	"time"

	"stablecoin-audit/pkg/config"
)

// Service integrates with external KYT providers and caches results.
type Service struct {
	cfg    config.ServiceConfig
	logger *slog.Logger
}

func NewService(cfg config.ServiceConfig, logger *slog.Logger) Service {
	return Service{cfg: cfg, logger: logger}
}

type scoreRequest struct {
	Address string `json:"address"`
}

type scoreResponse struct {
	Address string  `json:"address"`
	Risk    float64 `json:"risk"`
	TTL     int     `json:"ttl_seconds"`
}

func (s Service) handleScore(w http.ResponseWriter, r *http.Request) {
	var req scoreRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid payload", http.StatusBadRequest)
		return
	}
	risk := s.scoreAddress(req.Address)
	resp := scoreResponse{Address: req.Address, Risk: risk, TTL: int((5 * time.Minute).Seconds())}
	_ = json.NewEncoder(w).Encode(resp)
}

func (s Service) scoreAddress(address string) float64 {
	if address == "" {
		return 1.0
	}
	// TODO: call out to KYT provider and aggregate results.
	return 0.1
}
