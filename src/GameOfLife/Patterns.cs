namespace GameOfLife;

/// <summary>
/// Library of well-known Game of Life patterns.
/// </summary>
public static class Patterns
{
    /// <summary>
    /// Glider: a small spaceship that moves diagonally.
    /// Period 4, moves (1,1) every 4 generations.
    /// </summary>
    public static IReadOnlyList<Cell> Glider { get; } = new Cell[]
    {
        new(1, 0),
        new(2, 1),
        new(0, 2), new(1, 2), new(2, 2),
    };

    /// <summary>
    /// Blinker: the simplest oscillator, period 2.
    /// </summary>
    public static IReadOnlyList<Cell> Blinker { get; } = new Cell[]
    {
        new(0, 0), new(1, 0), new(2, 0),
    };

    /// <summary>
    /// Pulsar: a period-3 oscillator.
    /// </summary>
    public static IReadOnlyList<Cell> Pulsar { get; } = GeneratePulsar();

    /// <summary>
    /// Gosper Glider Gun: the first known finite pattern that grows without limit.
    /// Emits a new glider every 30 generations.
    /// </summary>
    public static IReadOnlyList<Cell> GosperGliderGun { get; } = new Cell[]
    {
        // Left square
        new(0, 4), new(0, 5),
        new(1, 4), new(1, 5),
        // Left part
        new(10, 4), new(10, 5), new(10, 6),
        new(11, 3), new(11, 7),
        new(12, 2), new(12, 8),
        new(13, 2), new(13, 8),
        new(14, 5),
        new(15, 3), new(15, 7),
        new(16, 4), new(16, 5), new(16, 6),
        new(17, 5),
        // Right part
        new(20, 2), new(20, 3), new(20, 4),
        new(21, 2), new(21, 3), new(21, 4),
        new(22, 1), new(22, 5),
        new(24, 0), new(24, 1), new(24, 5), new(24, 6),
        // Right square
        new(34, 2), new(34, 3),
        new(35, 2), new(35, 3),
    };

    /// <summary>
    /// Lightweight Spaceship (LWSS): moves horizontally, period 4.
    /// </summary>
    public static IReadOnlyList<Cell> LightweightSpaceship { get; } = new Cell[]
    {
        new(1, 0), new(4, 0),
        new(0, 1),
        new(0, 2), new(4, 2),
        new(0, 3), new(1, 3), new(2, 3), new(3, 3),
    };

    /// <summary>
    /// Returns a pattern by name (case-insensitive).
    /// </summary>
    public static IReadOnlyList<Cell>? GetByName(string name)
    {
        return name.ToLowerInvariant() switch
        {
            "glider" => Glider,
            "blinker" => Blinker,
            "pulsar" => Pulsar,
            "gosper" or "gosperglidergun" or "gun" => GosperGliderGun,
            "lwss" or "lightweightspaceship" or "spaceship" => LightweightSpaceship,
            _ => null,
        };
    }

    /// <summary>
    /// Returns all available pattern names.
    /// </summary>
    public static IReadOnlyList<string> AllNames { get; } = new[]
    {
        "glider", "blinker", "pulsar", "gosper", "lwss"
    };

    private static Cell[] GeneratePulsar()
    {
        // Pulsar is symmetric across both axes. Define one quadrant and reflect.
        var quadrant = new (int x, int y)[]
        {
            (2, 1), (3, 1), (4, 1),
            (1, 2), (6, 2),
            (1, 3), (6, 3),
            (1, 4), (6, 4),
            (2, 6), (3, 6), (4, 6),
        };

        var cells = new HashSet<Cell>();
        foreach (var (x, y) in quadrant)
        {
            // All four reflections around center (0,0) mapped to positive coords
            cells.Add(new Cell(x, y));
            cells.Add(new Cell(-x + 6, y));      // horizontal flip around center col 3
        }

        // Now reflect vertically: the pulsar is symmetric about row 3.5 effectively.
        // Easier to just define the full pattern directly.
        cells.Clear();

        // Top horizontal bars
        cells.Add(new Cell(2, 0)); cells.Add(new Cell(3, 0)); cells.Add(new Cell(4, 0));
        cells.Add(new Cell(8, 0)); cells.Add(new Cell(9, 0)); cells.Add(new Cell(10, 0));

        // Upper section vertical bars
        cells.Add(new Cell(0, 2)); cells.Add(new Cell(5, 2)); cells.Add(new Cell(7, 2)); cells.Add(new Cell(12, 2));
        cells.Add(new Cell(0, 3)); cells.Add(new Cell(5, 3)); cells.Add(new Cell(7, 3)); cells.Add(new Cell(12, 3));
        cells.Add(new Cell(0, 4)); cells.Add(new Cell(5, 4)); cells.Add(new Cell(7, 4)); cells.Add(new Cell(12, 4));

        // Middle horizontal bars
        cells.Add(new Cell(2, 5)); cells.Add(new Cell(3, 5)); cells.Add(new Cell(4, 5));
        cells.Add(new Cell(8, 5)); cells.Add(new Cell(9, 5)); cells.Add(new Cell(10, 5));

        // Mirrored: lower middle horizontal bars
        cells.Add(new Cell(2, 7)); cells.Add(new Cell(3, 7)); cells.Add(new Cell(4, 7));
        cells.Add(new Cell(8, 7)); cells.Add(new Cell(9, 7)); cells.Add(new Cell(10, 7));

        // Lower section vertical bars
        cells.Add(new Cell(0, 8)); cells.Add(new Cell(5, 8)); cells.Add(new Cell(7, 8)); cells.Add(new Cell(12, 8));
        cells.Add(new Cell(0, 9)); cells.Add(new Cell(5, 9)); cells.Add(new Cell(7, 9)); cells.Add(new Cell(12, 9));
        cells.Add(new Cell(0, 10)); cells.Add(new Cell(5, 10)); cells.Add(new Cell(7, 10)); cells.Add(new Cell(12, 10));

        // Bottom horizontal bars
        cells.Add(new Cell(2, 12)); cells.Add(new Cell(3, 12)); cells.Add(new Cell(4, 12));
        cells.Add(new Cell(8, 12)); cells.Add(new Cell(9, 12)); cells.Add(new Cell(10, 12));

        return cells.ToArray();
    }
}
