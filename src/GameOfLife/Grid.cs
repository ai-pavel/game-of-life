namespace GameOfLife;

/// <summary>
/// HashSet-based infinite grid implementing Conway's Game of Life rules.
/// </summary>
public class Grid
{
    private HashSet<Cell> _liveCells = new();

    public int Generation { get; private set; }

    public int Population => _liveCells.Count;

    public IReadOnlySet<Cell> LiveCells => _liveCells;

    public Grid() { }

    public Grid(IEnumerable<Cell> cells)
    {
        _liveCells = new HashSet<Cell>(cells);
    }

    public bool IsAlive(Cell cell) => _liveCells.Contains(cell);

    public void SetAlive(Cell cell) => _liveCells.Add(cell);

    public void SetDead(Cell cell) => _liveCells.Remove(cell);

    public void Clear()
    {
        _liveCells.Clear();
        Generation = 0;
    }

    /// <summary>
    /// Advances the grid by one generation using standard Conway rules:
    /// - A live cell with 2 or 3 neighbors survives.
    /// - A dead cell with exactly 3 neighbors becomes alive.
    /// - All other cells die or stay dead.
    /// </summary>
    public void Step()
    {
        var neighborCounts = new Dictionary<Cell, int>();

        foreach (var cell in _liveCells)
        {
            foreach (var neighbor in cell.Neighbors())
            {
                if (!neighborCounts.ContainsKey(neighbor))
                    neighborCounts[neighbor] = 0;
                neighborCounts[neighbor]++;
            }
        }

        var nextGeneration = new HashSet<Cell>();

        foreach (var (cell, count) in neighborCounts)
        {
            if (count == 3 || (count == 2 && _liveCells.Contains(cell)))
            {
                nextGeneration.Add(cell);
            }
        }

        _liveCells = nextGeneration;
        Generation++;
    }

    /// <summary>
    /// Advances the grid by the specified number of generations.
    /// </summary>
    public void Step(int generations)
    {
        for (int i = 0; i < generations; i++)
            Step();
    }

    /// <summary>
    /// Returns the bounding box of all live cells, or null if the grid is empty.
    /// </summary>
    public (int MinX, int MinY, int MaxX, int MaxY)? GetBounds()
    {
        if (_liveCells.Count == 0) return null;

        int minX = int.MaxValue, minY = int.MaxValue;
        int maxX = int.MinValue, maxY = int.MinValue;

        foreach (var cell in _liveCells)
        {
            if (cell.X < minX) minX = cell.X;
            if (cell.X > maxX) maxX = cell.X;
            if (cell.Y < minY) minY = cell.Y;
            if (cell.Y > maxY) maxY = cell.Y;
        }

        return (minX, minY, maxX, maxY);
    }

    /// <summary>
    /// Adds a pattern of cells at the given offset.
    /// </summary>
    public void AddPattern(IEnumerable<Cell> pattern, int offsetX = 0, int offsetY = 0)
    {
        foreach (var cell in pattern)
        {
            _liveCells.Add(new Cell(cell.X + offsetX, cell.Y + offsetY));
        }
    }
}
