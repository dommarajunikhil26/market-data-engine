# Market Data & Real-Time Alert Engine

A high-throughput, reactive market data processing engine built with Spring Boot and Project Reactor. The system establishes a single, resilient WebSocket connection to Binance, ingests real-time price ticks, and uses non-blocking reactive streams to calculate running averages and evaluate dynamic alert rules on the fly, pushing updates back to clients using Server-Sent Events (SSE).

---

## What It Does

This engine acts as a real-time bridge between external cryptocurrency exchanges and downstream clients.
* **Real-time Price Streaming:** Ingests live WebSocket feeds and streams individual symbol price changes directly to connected clients via SSE.
* **Rolling Metrics Engine:** Calculates stateless, infinite running averages for individual trading pairs (e.g., `BTCUSDT`) dynamically as ticks arrive.
* **Dynamic Alerting:** Evaluates streaming prices against user-defined alert thresholds (e.g., *Price of BTCUSDT goes ABOVE 55,000*) and instantly fires triggers downstream.

---

## System Architecture

To minimize connection overhead and respect exchange rate limits, the application maintains exactly **one** active WebSocket connection to Binance. Incoming data is fanned out internally to multiple independent, concurrent consumers using a reactive multicast processor.

```text
                  ┌────────────────────────┐
                  │   Binance WebSocket    │
                  └───────────┬────────────┘
                              │ Live JSON Ticks
                              ▼
               ┌──────────────────────────────┐
               │   BinanceWebSocketService    │
               │  (Parses into PriceTick DTO) │
               └──────────────┬───────────────┘
                              │
                              ▼
               ┌──────────────────────────────┐
               │    Sinks.Many<PriceTick>     │ (Hot Source / Multicast)
               └──────┬───────┬───────┬───────┘
                      │       │       │
       ┌──────────────┘       │       └──────────────┐
       ▼                      ▼                      ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│  Consumer 1  │       │  Consumer 2  │       │  Consumer 3  │
│  Raw Prices  │       │ Rolling Avg  │       │ Alert Engine │
│ (SSE Stream) │       │ (SSE Stream) │       │ (SSE Stream) │
└──────────────┘       └──────────────┘       └──────────────┘
```
## Key Technical Decisions

**Why a single WebSocket connection with `Sinks.many().multicast()`?**
Maintaining multiple connections to Binance would waste resources and risk rate limiting. A single connection feeds a reactive multicast sink — all consumers (persistence, alerts, SSE) receive every tick independently without coordination.

**Why `groupBy(symbol)` in the Alert Engine?**
Without grouping, a burst of BTC ticks could delay ETH alert evaluation. `groupBy` creates independent sub-streams per symbol, allowing each to be processed concurrently and rate-limited separately without cross-symbol interference.

**Why `scan` for rolling averages instead of buffering?**
`scan` is stateless and infinite — it computes a running average from the first tick onward without holding any data in memory. A buffer-based approach would require choosing a window size and holds all elements in memory until the window closes.

**Why SSE over WebSockets for client-facing streams?**
SSE is unidirectional (server → client), simpler to implement, and natively supported by browsers without additional libraries. Since clients only need to receive price updates — not send data — SSE is the correct fit.

**Why `boundedElastic` for JSON parsing?**
`objectMapper.readValue()` is a blocking call. Running it on the Netty event loop would block the thread handling all other connections. `subscribeOn(Schedulers.boundedElastic())` offloads parsing to a dedicated thread pool, keeping the event loop free.

## API Endpoints

### Alert Rules
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/alerts` | Create a new alert rule |
| GET | `/api/alerts` | List all alert rules |
| DELETE | `/api/alerts/{id}` | Delete an alert rule |

### Live Streams (SSE)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/streams/prices` | Live price ticks for all symbols |
| GET | `/api/streams/alerts` | Real-time triggered alerts |
| GET | `/api/streams/averages/{symbol}` | Rolling average for a symbol |

## Running Locally

**Prerequisites:** Docker, Java 21, Gradle

```bash
# Start MongoDB and Kafka
docker compose up -d

# Run the application
./gradlew bootRun
```

**Test live SSE streams:**
```bash
curl -N http://localhost:8080/api/streams/prices
curl -N http://localhost:8080/api/streams/averages/BTCUSDT
```