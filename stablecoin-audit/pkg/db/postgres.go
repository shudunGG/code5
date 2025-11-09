package db

import (
	"context"
	"database/sql"
	"time"
)

// NewPostgres opens a postgres connection with sane defaults for pooling.
// The actual driver registration should be performed by the caller.
func NewPostgres(ctx context.Context, dsn string) (*sql.DB, error) {
	db, err := sql.Open("postgres", dsn)
	if err != nil {
		return nil, err
	}
	db.SetMaxOpenConns(10)
	db.SetMaxIdleConns(5)
	db.SetConnMaxLifetime(30 * time.Minute)
	if err := db.PingContext(ctx); err != nil {
		return nil, err
	}
	return db, nil
}
