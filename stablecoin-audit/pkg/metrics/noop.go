package metrics

import "time"

// NoopProvider returns zeroed samples and is useful in scaffolding.
type NoopProvider struct{}

func (NoopProvider) Fetch(projectID uint64, metric string, window time.Duration) (Sample, error) {
	return Sample{Name: metric, ProjectID: projectID, Window: window}, nil
}
