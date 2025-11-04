import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { Rate } from 'k6/metrics';

const ERR_RATE = new Rate('errors');

// Environment variable to select which scenario to run: ASYNC_COROUTINE | SEQUENTIAL | BLOCKING_THREAD | ALL
const SELECTED = __ENV.STRATEGY || 'ALL';

// Reusable request function
function makePayment(strategy) {
  const url = 'http://localhost:9999/payments';
  const payload = JSON.stringify({
    correlationId: uuidv4(),
    amount: (Math.random() * 1000).toFixed(2),
    requestedAt: new Date().toISOString(),
  });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'strategy': strategy,
    },
    tags: { strategy }, // tag each request by strategy for easier grouping
  };

  const res = http.post(url, payload, params);

  const success = check(res, {
    'status is 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  ERR_RATE.add(!success);

  return res;
}

// Scenario definitions (adjust VUs/duration as needed)
const baseScenarios = {
  ASYNC_COROUTINE: {
    executor: 'constant-vus',
    exec: 'asyncCoroutineScenario',
    vus: 50,
    duration: '30s',
  },
  SEQUENTIAL: {
    executor: 'constant-vus',
    exec: 'sequentialScenario',
    vus: 20,
    duration: '30s',
  },
  BLOCKING_THREAD: {
    executor: 'constant-vus',
    exec: 'blockingThreadScenario',
    vus: 10,
    duration: '30s',
  },
};

// Build options.scenarios depending on SELECTED so each run can be isolated
export const options = {
  scenarios: (() => {
    if (SELECTED === 'ALL') {
      return baseScenarios;
    }
    if (baseScenarios[SELECTED]) {
      return { [SELECTED]: baseScenarios[SELECTED] };
    }
    // default: run all
    return baseScenarios;
  })(),

  // Thresholds example (tweak as needed)
  thresholds: {
    // keep error rate low
    'errors': ['rate<0.1'],
    // ensure p95 latency under a target (example)
    'http_req_duration': ['p(95)<5000'],
  },

  // Include standard percentiles in summary
  summaryTrendStats: ['min', 'avg', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

  // tags to help differentiate runs
  tags: { suite: 'payments-strategies' },
};

export function asyncCoroutineScenario() {
  makePayment('ASYNC_COROUTINE');
  sleep(1);
}

export function sequentialScenario() {
  makePayment('SEQUENTIAL');
  sleep(1);
}

export function blockingThreadScenario() {
  makePayment('BLOCKING_THREAD');
  sleep(1);
}
