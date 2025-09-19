package kafka

import "time"

// EventEnvelope mirrors the shared Kafka contract used across services.
type EventEnvelope struct {
	EventID   string      `json:"event_id"`
	Timestamp time.Time   `json:"ts"`
	Source    string      `json:"source"`
	ProjectID uint64      `json:"project_id"`
	Type      string      `json:"type"`
	Payload   interface{} `json:"payload"`
}

// NewEnvelope builds a new envelope with the supplied metadata.
func NewEnvelope(source, typ string, projectID uint64, payload interface{}) EventEnvelope {
	return EventEnvelope{
		EventID:   generateUUID(),
		Timestamp: time.Now().UTC(),
		Source:    source,
		ProjectID: projectID,
		Type:      typ,
		Payload:   payload,
	}
}
