package rules

import (
	"bufio"
	"errors"
	"fmt"
	"io/fs"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

// Loader reads YAML rule definitions and produces typed Rule structures.
type Loader struct {
	root string
}

func NewLoader(root string) Loader {
	return Loader{root: root}
}

func (l Loader) Load() ([]Rule, error) {
	var rules []Rule
	err := filepath.WalkDir(l.root, func(path string, entry fs.DirEntry, err error) error {
		if err != nil {
			return err
		}
		if entry.IsDir() || filepath.Ext(entry.Name()) != ".yaml" {
			return nil
		}
		rule, err := l.loadFile(path)
		if err != nil {
			return fmt.Errorf("load rule %s: %w", path, err)
		}
		rules = append(rules, rule)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return rules, nil
}

func (l Loader) loadFile(path string) (Rule, error) {
	file, err := os.Open(path)
	if err != nil {
		return Rule{}, err
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	rule := Rule{}
	var section string
	var currentClause *Clause
	var currentEvidence *Evidence
	var currentAction *Action

	lineNo := 0
	for scanner.Scan() {
		lineNo++
		rawLine := scanner.Text()
		trimmed := strings.TrimSpace(rawLine)
		if trimmed == "" || strings.HasPrefix(trimmed, "#") {
			continue
		}
		indent := countIndent(rawLine)

		if indent == 0 {
			key, value, err := splitKV(trimmed)
			if err != nil {
				return Rule{}, fmt.Errorf("%s:%d: %w", path, lineNo, err)
			}
			if value == nil {
				section = key
				continue
			}
			switch key {
			case "id":
				rule.ID = value.str
			case "name":
				rule.Name = value.str
			case "severity":
				rule.Severity = value.str
			case "scope":
				rule.Scope = value.str
			default:
				return Rule{}, fmt.Errorf("%s:%d: unknown key %s", path, lineNo, key)
			}
			continue
		}

		switch section {
		case "when":
			if strings.HasPrefix(trimmed, "- ") {
				rule.Clauses = append(rule.Clauses, Clause{})
				currentClause = &rule.Clauses[len(rule.Clauses)-1]
				trimmed = strings.TrimPrefix(trimmed, "- ")
				if trimmed != "" {
					if err := assignClauseField(currentClause, trimmed); err != nil {
						return Rule{}, fmt.Errorf("%s:%d: %w", path, lineNo, err)
					}
				}
				continue
			}
			if currentClause == nil {
				return Rule{}, fmt.Errorf("%s:%d: clause attribute without entry", path, lineNo)
			}
			if err := assignClauseField(currentClause, trimmed); err != nil {
				return Rule{}, fmt.Errorf("%s:%d: %w", path, lineNo, err)
			}
		case "evidence":
			if strings.HasPrefix(trimmed, "- ") {
				rule.Evidence = append(rule.Evidence, Evidence{})
				currentEvidence = &rule.Evidence[len(rule.Evidence)-1]
				trimmed = strings.TrimPrefix(trimmed, "- ")
				if trimmed != "" {
					if err := assignEvidenceField(currentEvidence, trimmed); err != nil {
						return Rule{}, fmt.Errorf("%s:%d: %w", path, lineNo, err)
					}
				}
				continue
			}
			if currentEvidence == nil {
				return Rule{}, fmt.Errorf("%s:%d: evidence attribute without entry", path, lineNo)
			}
			if err := assignEvidenceField(currentEvidence, trimmed); err != nil {
				return Rule{}, fmt.Errorf("%s:%d: %w", path, lineNo, err)
			}
		case "action":
			if strings.HasPrefix(trimmed, "- ") {
				rule.Actions = append(rule.Actions, Action{})
				currentAction = &rule.Actions[len(rule.Actions)-1]
				trimmed = strings.TrimPrefix(trimmed, "- ")
				if trimmed != "" {
					if err := assignActionField(currentAction, trimmed); err != nil {
						return Rule{}, fmt.Errorf("%s:%d: %w", path, lineNo, err)
					}
				}
				continue
			}
			if currentAction == nil {
				return Rule{}, fmt.Errorf("%s:%d: action attribute without entry", path, lineNo)
			}
			if err := assignActionField(currentAction, trimmed); err != nil {
				return Rule{}, fmt.Errorf("%s:%d: %w", path, lineNo, err)
			}
		default:
			return Rule{}, fmt.Errorf("%s:%d: unexpected section %s", path, lineNo, section)
		}
	}
	if err := scanner.Err(); err != nil {
		return Rule{}, err
	}
	if rule.ID == "" {
		return Rule{}, errors.New("rule missing id")
	}
	return rule, nil
}

type valueHolder struct {
	str   string
	float *float64
	bool  *bool
}

func splitKV(line string) (string, *valueHolder, error) {
	parts := strings.SplitN(line, ":", 2)
	if len(parts) != 2 {
		return "", nil, fmt.Errorf("invalid key-value: %s", line)
	}
	key := strings.TrimSpace(parts[0])
	value := strings.TrimSpace(parts[1])
	if value == "" {
		return key, nil, nil
	}
	holder := valueHolder{str: trimQuotes(value)}
	if f, err := strconv.ParseFloat(holder.str, 64); err == nil {
		holder.float = &f
	}
	if b, err := strconv.ParseBool(strings.ToLower(holder.str)); err == nil {
		holder.bool = &b
	}
	return key, &holder, nil
}

func assignClauseField(clause *Clause, line string) error {
	key, val, err := splitKV(line)
	if err != nil {
		return err
	}
	if val == nil {
		return fmt.Errorf("missing value for %s", key)
	}
	switch key {
	case "metric":
		clause.Metric = val.str
	case "op":
		clause.Op = val.str
	case "value":
		if val.float == nil {
			return fmt.Errorf("value must be float")
		}
		clause.Value = *val.float
	case "window":
		d, err := time.ParseDuration(val.str)
		if err != nil {
			return err
		}
		clause.Window = d
	default:
		return fmt.Errorf("unknown clause key %s", key)
	}
	return nil
}

func assignEvidenceField(ev *Evidence, line string) error {
	key, val, err := splitKV(line)
	if err != nil {
		return err
	}
	if val == nil {
		return fmt.Errorf("missing value for %s", key)
	}
	switch key {
	case "type":
		ev.Type = val.str
	case "ref":
		ev.Ref = val.str
	default:
		return fmt.Errorf("unknown evidence key %s", key)
	}
	return nil
}

func assignActionField(action *Action, line string) error {
	key, val, err := splitKV(line)
	if err != nil {
		return err
	}
	if val == nil {
		return fmt.Errorf("missing value for %s", key)
	}
	switch key {
	case "emit_alert":
		if val.bool == nil {
			return fmt.Errorf("emit_alert must be boolean")
		}
		action.EmitAlert = *val.bool
	case "tags":
		action.Tags = parseList(val.str)
	default:
		return fmt.Errorf("unknown action key %s", key)
	}
	return nil
}

func trimQuotes(in string) string {
	in = strings.TrimSpace(in)
	if strings.HasPrefix(in, "\"") && strings.HasSuffix(in, "\"") {
		return strings.Trim(in, "\"")
	}
	if strings.HasPrefix(in, "[") && strings.HasSuffix(in, "]") {
		return in
	}
	if strings.HasPrefix(in, "'") && strings.HasSuffix(in, "'") {
		return strings.Trim(in, "'")
	}
	return in
}

func parseList(value string) []string {
	trimmed := strings.TrimSpace(value)
	trimmed = strings.TrimPrefix(trimmed, "[")
	trimmed = strings.TrimSuffix(trimmed, "]")
	if trimmed == "" {
		return nil
	}
	parts := strings.Split(trimmed, ",")
	result := make([]string, 0, len(parts))
	for _, p := range parts {
		result = append(result, strings.TrimSpace(strings.Trim(p, "\"'")))
	}
	return result
}

func countIndent(line string) int {
	count := 0
	for _, r := range line {
		if r == ' ' {
			count++
		} else {
			break
		}
	}
	return count
}
