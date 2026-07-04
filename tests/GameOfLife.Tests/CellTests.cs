using GameOfLife;

namespace GameOfLife.Tests;

public class CellTests
{
    [Fact]
    public void Cell_Equality()
    {
        var a = new Cell(1, 2);
        var b = new Cell(1, 2);
        Assert.Equal(a, b);
    }

    [Fact]
    public void Cell_Inequality()
    {
        var a = new Cell(1, 2);
        var b = new Cell(3, 4);
        Assert.NotEqual(a, b);
    }

    [Fact]
    public void Neighbors_Returns8Cells()
    {
        var cell = new Cell(5, 5);
        var neighbors = cell.Neighbors().ToList();
        Assert.Equal(8, neighbors.Count);
        Assert.DoesNotContain(cell, neighbors);
    }

    [Fact]
    public void Neighbors_AreAdjacent()
    {
        var cell = new Cell(0, 0);
        var neighbors = cell.Neighbors().ToList();
        Assert.Contains(new Cell(-1, -1), neighbors);
        Assert.Contains(new Cell(0, -1), neighbors);
        Assert.Contains(new Cell(1, -1), neighbors);
        Assert.Contains(new Cell(-1, 0), neighbors);
        Assert.Contains(new Cell(1, 0), neighbors);
        Assert.Contains(new Cell(-1, 1), neighbors);
        Assert.Contains(new Cell(0, 1), neighbors);
        Assert.Contains(new Cell(1, 1), neighbors);
    }
}
