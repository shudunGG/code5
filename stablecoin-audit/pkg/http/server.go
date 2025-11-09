package http

import (
	"context"
	"net/http"
	"time"
)

// Server wraps a net/http server with graceful shutdown semantics.
type Server struct {
	Server *http.Server
}

// New creates an HTTP server with timeout defaults.
func New(addr string, handler http.Handler) Server {
	return Server{Server: &http.Server{
		Addr:              addr,
		Handler:           handler,
		ReadHeaderTimeout: 5 * time.Second,
		ReadTimeout:       15 * time.Second,
		WriteTimeout:      15 * time.Second,
	}}
}

// Run starts listening until the context is cancelled.
func (s Server) Run(ctx context.Context) error {
	done := make(chan error, 1)
	go func() {
		done <- s.Server.ListenAndServe()
	}()
	select {
	case <-ctx.Done():
		shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_ = s.Server.Shutdown(shutdownCtx)
		return ctx.Err()
	case err := <-done:
		return err
	}
}
