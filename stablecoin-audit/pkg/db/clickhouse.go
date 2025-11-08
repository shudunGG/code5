package db

import "context"

// ClickHouseConnector is a thin interface that allows substituting real connections in tests.
type ClickHouseConnector interface {
	Exec(ctx context.Context, query string, args ...any) error
}
