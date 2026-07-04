using System.Text;

namespace GameOfLife;

/// <summary>
/// Console renderer that animates the Game of Life grid in the terminal.
/// </summary>
public class Renderer
{
    private const char AliveChar = '\u2588'; // Full block
    private const char DeadChar = ' ';

    public int ViewportWidth { get; set; } = 80;
    public int ViewportHeight { get; set; } = 24;
    public int OffsetX { get; set; }
    public int OffsetY { get; set; }

    /// <summary>
    /// Renders the grid to a string for the current viewport.
    /// </summary>
    public string Render(Grid grid)
    {
        var sb = new StringBuilder();

        for (int y = OffsetY; y < OffsetY + ViewportHeight; y++)
        {
            for (int x = OffsetX; x < OffsetX + ViewportWidth; x++)
            {
                sb.Append(grid.IsAlive(new Cell(x, y)) ? AliveChar : DeadChar);
            }
            if (y < OffsetY + ViewportHeight - 1)
                sb.AppendLine();
        }

        return sb.ToString();
    }

    /// <summary>
    /// Renders the grid to the console, clearing the screen first.
    /// </summary>
    public void RenderToConsole(Grid grid)
    {
        Console.SetCursorPosition(0, 0);
        Console.Write(Render(grid));
        Console.WriteLine();
        Console.WriteLine($"Generation: {grid.Generation}  Population: {grid.Population}    ");
    }

    /// <summary>
    /// Animates the grid in the console for the given number of generations.
    /// </summary>
    public void Animate(Grid grid, int generations = int.MaxValue, int delayMs = 100, CancellationToken ct = default)
    {
        Console.Clear();
        Console.CursorVisible = false;

        // Auto-center viewport on the pattern
        CenterOnPattern(grid);

        try
        {
            for (int i = 0; i < generations && !ct.IsCancellationRequested; i++)
            {
                RenderToConsole(grid);
                Thread.Sleep(delayMs);
                grid.Step();
            }
        }
        finally
        {
            Console.CursorVisible = true;
        }
    }

    /// <summary>
    /// Centers the viewport on the current live cells.
    /// </summary>
    public void CenterOnPattern(Grid grid)
    {
        var bounds = grid.GetBounds();
        if (bounds == null) return;

        var (minX, minY, maxX, maxY) = bounds.Value;
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;

        OffsetX = centerX - ViewportWidth / 2;
        OffsetY = centerY - ViewportHeight / 2;
    }
}
