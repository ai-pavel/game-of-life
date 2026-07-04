# Conway's Game of Life

A C# .NET 8 implementation of Conway's Game of Life with a HashSet-based infinite grid, pattern library, RLE import/export, and terminal animation.

## Project Structure

```
src/
  GameOfLife/          # Core library
    Cell.cs            # Cell position record
    Grid.cs            # Infinite grid with Conway's rules
    Patterns.cs        # Well-known pattern library
    RleParser.cs       # RLE format import/export
    Renderer.cs        # Console terminal renderer
  GameOfLife.Cli/      # Console application
    Program.cs         # CLI entry point
tests/
  GameOfLife.Tests/    # xUnit tests
```

## Build

```bash
dotnet build
```

## Run Tests

```bash
dotnet test
```

## CLI Usage

```bash
# Animate a pattern in the terminal (Ctrl+C to stop)
dotnet run --project src/GameOfLife.Cli -- run glider
dotnet run --project src/GameOfLife.Cli -- run gosper --delay 50

# Load and animate an RLE file
dotnet run --project src/GameOfLife.Cli -- load pattern.rle

# Export a pattern to RLE format
dotnet run --project src/GameOfLife.Cli -- export glider -o glider.rle

# Advance a pattern by N generations and print result
dotnet run --project src/GameOfLife.Cli -- step blinker -n 10

# List available patterns
dotnet run --project src/GameOfLife.Cli -- patterns
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
