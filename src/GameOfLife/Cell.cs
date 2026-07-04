namespace GameOfLife;

/// <summary>
/// Represents a cell position on the infinite grid.
/// </summary>
public readonly record struct Cell(int X, int Y)
{
    /// <summary>
    /// Returns the eight neighboring cell positions.
    /// </summary>
    public IEnumerable<Cell> Neighbors()
    {
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dy = -1; dy <= 1; dy++)
            {
                if (dx == 0 && dy == 0) continue;
                yield return new Cell(X + dx, Y + dy);
            }
        }
    }
}
