using System.Text;
using System.Text.RegularExpressions;

namespace GameOfLife;

/// <summary>
/// Parses and exports patterns in Run Length Encoded (RLE) format.
/// See: https://conwaylife.com/wiki/Run_Length_Encoded
/// </summary>
public static class RleParser
{
    /// <summary>
    /// Parses an RLE string and returns the set of live cells.
    /// </summary>
    public static List<Cell> Parse(string rle)
    {
        var cells = new List<Cell>();
        var lines = rle.Split('\n')
            .Select(l => l.Trim())
            .Where(l => !string.IsNullOrEmpty(l))
            .ToList();

        // Skip comment lines (#) and extract header
        var dataLines = new List<string>();
        foreach (var line in lines)
        {
            if (line.StartsWith('#'))
                continue;
            dataLines.Add(line);
        }

        if (dataLines.Count == 0)
            return cells;

        int startIndex = 0;
        // Skip header line (x = ..., y = ..., rule = ...)
        if (dataLines[0].StartsWith("x", StringComparison.OrdinalIgnoreCase)
            && dataLines[0].Contains('='))
        {
            startIndex = 1;
        }

        // Concatenate remaining data lines into one string
        var data = string.Join("", dataLines.Skip(startIndex));

        // Parse the RLE data
        int x = 0, y = 0;
        int runCount = 0;

        foreach (char c in data)
        {
            if (c == '!')
                break;

            if (char.IsDigit(c))
            {
                runCount = runCount * 10 + (c - '0');
                continue;
            }

            int count = runCount == 0 ? 1 : runCount;
            runCount = 0;

            if (c == 'b')
            {
                // Dead cells
                x += count;
            }
            else if (c == 'o')
            {
                // Live cells
                for (int i = 0; i < count; i++)
                {
                    cells.Add(new Cell(x, y));
                    x++;
                }
            }
            else if (c == '$')
            {
                // End of row(s)
                y += count;
                x = 0;
            }
        }

        return cells;
    }

    /// <summary>
    /// Parses an RLE file and returns the set of live cells.
    /// </summary>
    public static List<Cell> ParseFile(string filePath)
    {
        var content = File.ReadAllText(filePath);
        return Parse(content);
    }

    /// <summary>
    /// Exports a set of live cells to RLE format.
    /// </summary>
    public static string Export(IEnumerable<Cell> cells)
    {
        var cellSet = new HashSet<Cell>(cells);
        if (cellSet.Count == 0)
            return "x = 0, y = 0, rule = B3/S23\n!\n";

        int minX = cellSet.Min(c => c.X);
        int minY = cellSet.Min(c => c.Y);
        int maxX = cellSet.Max(c => c.X);
        int maxY = cellSet.Max(c => c.Y);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        var sb = new StringBuilder();
        sb.AppendLine($"x = {width}, y = {height}, rule = B3/S23");

        var dataSb = new StringBuilder();
        for (int row = minY; row <= maxY; row++)
        {
            int col = minX;
            while (col <= maxX)
            {
                bool alive = cellSet.Contains(new Cell(col, row));
                char tag = alive ? 'o' : 'b';
                int run = 0;

                while (col <= maxX && cellSet.Contains(new Cell(col, row)) == alive)
                {
                    run++;
                    col++;
                }

                // Omit trailing dead cells on each row
                if (!alive && col > maxX)
                    break;

                if (run == 1)
                    dataSb.Append(tag);
                else
                    dataSb.Append($"{run}{tag}");
            }

            if (row < maxY)
                dataSb.Append('$');
        }

        dataSb.Append('!');

        // Wrap data lines at 70 characters
        string data = dataSb.ToString();
        for (int i = 0; i < data.Length; i += 70)
        {
            int len = Math.Min(70, data.Length - i);
            sb.AppendLine(data.Substring(i, len));
        }

        return sb.ToString();
    }

    /// <summary>
    /// Exports a Grid's live cells to RLE format.
    /// </summary>
    public static string Export(Grid grid)
    {
        return Export(grid.LiveCells);
    }

    /// <summary>
    /// Exports a Grid's live cells to an RLE file.
    /// </summary>
    public static void ExportToFile(Grid grid, string filePath)
    {
        File.WriteAllText(filePath, Export(grid));
    }
}
