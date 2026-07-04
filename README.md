# Conway's Game of Life

[![CI](https://github.com/ai-pavel/game-of-life/actions/workflows/ci.yml/badge.svg)](https://github.com/ai-pavel/game-of-life/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/ai-pavel/game-of-life/branch/main/graph/badge.svg)](https://codecov.io/gh/ai-pavel/game-of-life)

A Clojure implementation of Conway's Game of Life with a set-based infinite grid, pattern library, RLE import/export, and terminal animation.

## Project Structure

```
src/game_of_life/
  core.clj        # CLI entry point
  grid.clj        # Infinite grid with Conway's rules
  patterns.clj    # Well-known pattern library
  rle.clj         # RLE format import/export
  renderer.clj    # Console terminal renderer
test/game_of_life/
  grid_test.clj     # Grid logic tests
  rle_test.clj      # RLE parser tests
  patterns_test.clj # Pattern library tests
```

## Build

```bash
lein compile
```

## Run Tests

```bash
lein test
```

## CLI Usage

```bash
# Animate a pattern in the terminal (Ctrl+C to stop)
lein run run glider
lein run run gosper --delay 50

# Load and animate an RLE file
lein run load pattern.rle

# Export a pattern to RLE format
lein run export glider -o glider.rle

# Advance a pattern by N generations and print result
lein run step blinker -n 10

# List available patterns
lein run patterns
```

## Patterns

| Name    | Description                        | Cells |
|---------|------------------------------------|-------|
| glider  | Small diagonal spaceship           | 5     |
| blinker | Period-2 oscillator                | 3     |
| pulsar  | Period-3 oscillator                | 48    |
| gosper  | Gosper glider gun (infinite growth)| 36    |
| lwss    | Lightweight spaceship (horizontal) | 9     |

## RLE Format

The Run Length Encoded format is the standard for sharing Game of Life patterns. Example (glider):

```
#N Glider
x = 3, y = 3, rule = B3/S23
bo$2bo$3o!
```
