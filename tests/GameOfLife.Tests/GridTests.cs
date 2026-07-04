using GameOfLife;

namespace GameOfLife.Tests;

public class GridTests
{
    [Fact]
    public void EmptyGrid_HasZeroPopulation()
    {
        var grid = new Grid();
        Assert.Equal(0, grid.Population);
        Assert.Equal(0, grid.Generation);
    }

    [Fact]
    public void SetAlive_IncreasesPopulation()
    {
        var grid = new Grid();
        grid.SetAlive(new Cell(0, 0));
        Assert.Equal(1, grid.Population);
        Assert.True(grid.IsAlive(new Cell(0, 0)));
    }

    [Fact]
    public void SetDead_DecreasesPopulation()
    {
        var grid = new Grid();
        grid.SetAlive(new Cell(0, 0));
        grid.SetDead(new Cell(0, 0));
        Assert.Equal(0, grid.Population);
    }

    [Fact]
    public void Step_IncrementsGeneration()
    {
        var grid = new Grid();
        grid.Step();
        Assert.Equal(1, grid.Generation);
    }

    [Fact]
    public void Blinker_OscillatesPeriod2()
    {
        var grid = new Grid();
        grid.AddPattern(Patterns.Blinker);

        // Phase 1: horizontal
        Assert.True(grid.IsAlive(new Cell(0, 0)));
        Assert.True(grid.IsAlive(new Cell(1, 0)));
        Assert.True(grid.IsAlive(new Cell(2, 0)));

        grid.Step();

        // Phase 2: vertical
        Assert.True(grid.IsAlive(new Cell(1, -1)));
        Assert.True(grid.IsAlive(new Cell(1, 0)));
        Assert.True(grid.IsAlive(new Cell(1, 1)));
        Assert.Equal(3, grid.Population);

        grid.Step();

        // Back to phase 1
        Assert.True(grid.IsAlive(new Cell(0, 0)));
        Assert.True(grid.IsAlive(new Cell(1, 0)));
        Assert.True(grid.IsAlive(new Cell(2, 0)));
        Assert.Equal(3, grid.Population);
    }

    [Fact]
    public void Block_IsStillLife()
    {
        // A 2x2 block should remain unchanged
        var grid = new Grid();
        grid.SetAlive(new Cell(0, 0));
        grid.SetAlive(new Cell(1, 0));
        grid.SetAlive(new Cell(0, 1));
        grid.SetAlive(new Cell(1, 1));

        grid.Step();

        Assert.Equal(4, grid.Population);
        Assert.True(grid.IsAlive(new Cell(0, 0)));
        Assert.True(grid.IsAlive(new Cell(1, 0)));
        Assert.True(grid.IsAlive(new Cell(0, 1)));
        Assert.True(grid.IsAlive(new Cell(1, 1)));
    }

    [Fact]
    public void LoneCell_Dies()
    {
        var grid = new Grid();
        grid.SetAlive(new Cell(5, 5));
        grid.Step();
        Assert.Equal(0, grid.Population);
    }

    [Fact]
    public void Step_MultipleGenerations()
    {
        var grid = new Grid();
        grid.AddPattern(Patterns.Blinker);
        grid.Step(10);
        Assert.Equal(10, grid.Generation);
        Assert.Equal(3, grid.Population);
    }

    [Fact]
    public void GetBounds_ReturnsCorrectBounds()
    {
        var grid = new Grid();
        grid.SetAlive(new Cell(-5, 3));
        grid.SetAlive(new Cell(10, -2));

        var bounds = grid.GetBounds();
        Assert.NotNull(bounds);
        Assert.Equal(-5, bounds.Value.MinX);
        Assert.Equal(-2, bounds.Value.MinY);
        Assert.Equal(10, bounds.Value.MaxX);
        Assert.Equal(3, bounds.Value.MaxY);
    }

    [Fact]
    public void GetBounds_EmptyGrid_ReturnsNull()
    {
        var grid = new Grid();
        Assert.Null(grid.GetBounds());
    }

    [Fact]
    public void Clear_ResetsGrid()
    {
        var grid = new Grid();
        grid.AddPattern(Patterns.Glider);
        grid.Step(5);
        grid.Clear();
        Assert.Equal(0, grid.Population);
        Assert.Equal(0, grid.Generation);
    }

    [Fact]
    public void AddPattern_WithOffset()
    {
        var grid = new Grid();
        grid.AddPattern(Patterns.Blinker, 10, 20);
        Assert.True(grid.IsAlive(new Cell(10, 20)));
        Assert.True(grid.IsAlive(new Cell(11, 20)));
        Assert.True(grid.IsAlive(new Cell(12, 20)));
    }

    [Fact]
    public void Glider_MovesAfter4Steps()
    {
        var grid = new Grid();
        grid.AddPattern(Patterns.Glider);
        grid.Step(4);
        Assert.Equal(5, grid.Population);
        // Glider should have moved (1,1) after 4 generations
        Assert.True(grid.IsAlive(new Cell(2, 1)));
        Assert.True(grid.IsAlive(new Cell(3, 2)));
        Assert.True(grid.IsAlive(new Cell(1, 3)));
        Assert.True(grid.IsAlive(new Cell(2, 3)));
        Assert.True(grid.IsAlive(new Cell(3, 3)));
    }
}
