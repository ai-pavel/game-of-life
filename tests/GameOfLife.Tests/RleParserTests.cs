using GameOfLife;

namespace GameOfLife.Tests;

public class RleParserTests
{
    [Fact]
    public void Parse_Glider()
    {
        string rle = @"#N Glider
#C A small spaceship
x = 3, y = 3, rule = B3/S23
bo$2bo$3o!";

        var cells = RleParser.Parse(rle);
        Assert.Equal(5, cells.Count);
        Assert.Contains(new Cell(1, 0), cells);
        Assert.Contains(new Cell(2, 1), cells);
        Assert.Contains(new Cell(0, 2), cells);
        Assert.Contains(new Cell(1, 2), cells);
        Assert.Contains(new Cell(2, 2), cells);
    }

    [Fact]
    public void Parse_Blinker()
    {
        string rle = "x = 3, y = 1, rule = B3/S23\n3o!";
        var cells = RleParser.Parse(rle);
        Assert.Equal(3, cells.Count);
        Assert.Contains(new Cell(0, 0), cells);
        Assert.Contains(new Cell(1, 0), cells);
        Assert.Contains(new Cell(2, 0), cells);
    }

    [Fact]
    public void Parse_WithRunCounts()
    {
        // 5 alive cells in a row
        string rle = "x = 5, y = 1\n5o!";
        var cells = RleParser.Parse(rle);
        Assert.Equal(5, cells.Count);
    }

    [Fact]
    public void Parse_MultipleRows()
    {
        // 2x2 block
        string rle = "x = 2, y = 2\n2o$2o!";
        var cells = RleParser.Parse(rle);
        Assert.Equal(4, cells.Count);
        Assert.Contains(new Cell(0, 0), cells);
        Assert.Contains(new Cell(1, 0), cells);
        Assert.Contains(new Cell(0, 1), cells);
        Assert.Contains(new Cell(1, 1), cells);
    }

    [Fact]
    public void Parse_SkipsMultipleRows()
    {
        // Cell at (0,0) and (0,3)
        string rle = "x = 1, y = 4\no3$o!";
        var cells = RleParser.Parse(rle);
        Assert.Equal(2, cells.Count);
        Assert.Contains(new Cell(0, 0), cells);
        Assert.Contains(new Cell(0, 3), cells);
    }

    [Fact]
    public void Export_AndReparse_Roundtrip()
    {
        var original = new List<Cell>
        {
            new(1, 0),
            new(2, 1),
            new(0, 2), new(1, 2), new(2, 2),
        };

        string rle = RleParser.Export(original);
        var parsed = RleParser.Parse(rle);

        // Normalize both to sorted lists for comparison
        var origSorted = original.OrderBy(c => c.Y).ThenBy(c => c.X).ToList();
        var parsedNormalized = parsed
            .Select(c => new Cell(c.X, c.Y))
            .OrderBy(c => c.Y).ThenBy(c => c.X).ToList();

        Assert.Equal(origSorted.Count, parsedNormalized.Count);
        for (int i = 0; i < origSorted.Count; i++)
        {
            // The export normalizes to min corner = (0,0)
            Assert.Equal(origSorted[i].X, parsedNormalized[i].X);
            Assert.Equal(origSorted[i].Y, parsedNormalized[i].Y);
        }
    }

    [Fact]
    public void Export_EmptyCells_ReturnsValidRle()
    {
        string rle = RleParser.Export(Array.Empty<Cell>());
        Assert.Contains("x = 0, y = 0", rle);
        Assert.Contains("!", rle);
    }

    [Fact]
    public void Export_ContainsHeader()
    {
        var cells = Patterns.Blinker;
        string rle = RleParser.Export(cells);
        Assert.Contains("x = 3, y = 1", rle);
        Assert.Contains("rule = B3/S23", rle);
    }

    [Fact]
    public void Parse_NoHeader()
    {
        // RLE data without a header line
        string rle = "3o!";
        var cells = RleParser.Parse(rle);
        Assert.Equal(3, cells.Count);
    }
}
