package rules

import "time"

// Rule describes a compiled DSL rule that the engine can evaluate.
type Rule struct {
	ID       string
	Name     string
	Severity string
	Scope    string
	Clauses  []Clause
	Evidence []Evidence
	Actions  []Action
}

// Clause defines a single metric threshold evaluation.
type Clause struct {
	Metric string
	Op     string
	Value  float64
	Window time.Duration
}

// Evidence captures references that should be attached to alerts.
type Evidence struct {
	Type string
	Ref  string
}

// Action enumerates alert behavior toggles.
type Action struct {
	EmitAlert bool
	Tags      []string
}
