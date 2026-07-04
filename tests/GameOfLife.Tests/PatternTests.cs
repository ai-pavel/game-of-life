using GameOfLife;

namespace GameOfLife.Tests;

public class PatternTests
{
    [Fact]
    public void Glider_Has5Cells()
    {
        Assert.Equal(5, Patterns.Glider.Count);
    }

    [Fact]
    public void Blinker_Has3Cells()
    {
        Assert.Equal(3, Patterns.Blinker.Count);
    }

    [Fact]
    public void Pulsar_Has48Cells()
    {
        Assert.Equal(48, Patterns.Pulsar.Count);
    }

    [Fact]
    public void GosperGliderGun_Has36Cells()
    {
        Assert.Equal(36, Patterns.GosperGliderGun.Count);
    }

    [Fact]
    public void LightweightSpaceship_Has9Cells()
    {
        Assert.Equal(9, Patterns.LightweightSpaceship.Count);
    }

    [Fact]
    public void GetByName_ReturnsPattern()
    {
        Assert.NotNull(Patterns.GetByName("glider"));
        Assert.NotNull(Patterns.GetByName("BLINKER"));
        Assert.NotNull(Patterns.GetByName("Pulsar"));
        Assert.NotNull(Patterns.GetByName("gosper"));
        Assert.NotNull(Patterns.GetByName("lwss"));
    }

    [Fact]
    public void GetByName_UnknownReturnsNull()
    {
        Assert.Null(Patterns.GetByName("nonexistent"));
    }

    [Theory]
    [InlineData("glider")]
    [InlineData("blinker")]
    [InlineData("pulsar")]
    [InlineData("gosper")]
    [InlineData("lwss")]
    public void AllPatterns_AreNonEmpty(string name)
    {
        var pattern = Patterns.GetByName(name);
        Assert.NotNull(pattern);
        Assert.True(pattern.Count > 0);
    }
}
