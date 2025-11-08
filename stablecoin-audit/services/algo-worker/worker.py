"""Algo worker entry point for anomaly detection tasks."""
from __future__ import annotations

import json
import math
from dataclasses import dataclass
from typing import Iterable, List


@dataclass
class PriceSample:
    timestamp: float
    value: float


def ema(values: Iterable[float], span: int) -> List[float]:
    alpha = 2 / (span + 1)
    result: List[float] = []
    prev = None
    for value in values:
        if prev is None:
            prev = value
        else:
            prev = alpha * value + (1 - alpha) * prev
        result.append(prev)
    return result


def depeg_index(samples: Iterable[PriceSample], anchor: float = 1.0) -> float:
    ratios = [abs(sample.value - anchor) / anchor for sample in samples]
    if not ratios:
        return 0.0
    smooth = ema(ratios, span=20)
    return smooth[-1]


def consume_event(event: str) -> None:
    payload = json.loads(event)
    prices = [PriceSample(timestamp=item["ts"], value=item["price"]) for item in payload["prices"]]
    score = depeg_index(prices)
    if math.isfinite(score):
        print(json.dumps({"score": score, "rule_id": "ALG-DEPEG"}))


if __name__ == "__main__":
    example = json.dumps({"prices": [{"ts": 0, "price": 1.0}, {"ts": 60, "price": 0.995}]})
    consume_event(example)
