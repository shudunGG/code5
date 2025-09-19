package metrics

import "time"

// Sample represents a metric datapoint consumed by the rule engine.
type Sample struct {
	Name      string
	ProjectID uint64
	Value     float64
	Window    time.Duration
	Timestamp time.Time
}

// Provider describes how rule-engine obtains metric samples.
type Provider interface {
	Fetch(projectID uint64, metric string, window time.Duration) (Sample, error)
}
