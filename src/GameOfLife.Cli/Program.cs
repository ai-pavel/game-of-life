using GameOfLife;

var args = Environment.GetCommandLineArgs().Skip(1).ToArray();

if (args.Length == 0)
{
    PrintUsage();
    return;
}

string command = args[0].ToLowerInvariant();

switch (command)
{
    case "run":
        RunCommand(args.Skip(1).ToArray());
        break;
    case "load":
        LoadCommand(args.Skip(1).ToArray());
        break;
    case "export":
        ExportCommand(args.Skip(1).ToArray());
        break;
    case "step":
        StepCommand(args.Skip(1).ToArray());
        break;
    case "patterns":
        ListPatterns();
        break;
    case "help":
    case "--help":
    case "-h":
        PrintUsage();
        break;
    default:
        Console.Error.WriteLine($"Unknown command: {command}");
        PrintUsage();
        break;
}

void RunCommand(string[] runArgs)
{
    string patternName = runArgs.Length > 0 ? runArgs[0] : "glider";
    int delay = 100;
    int generations = int.MaxValue;

    for (int i = 1; i < runArgs.Length; i++)
    {
        if ((runArgs[i] == "--delay" || runArgs[i] == "-d") && i + 1 < runArgs.Length)
            delay = int.Parse(runArgs[++i]);
        else if ((runArgs[i] == "--generations" || runArgs[i] == "-g") && i + 1 < runArgs.Length)
            generations = int.Parse(runArgs[++i]);
    }

    var pattern = Patterns.GetByName(patternName);
    if (pattern == null)
    {
        Console.Error.WriteLine($"Unknown pattern: {patternName}");
        Console.Error.WriteLine($"Available patterns: {string.Join(", ", Patterns.AllNames)}");
        return;
    }

    var grid = new Grid();
    grid.AddPattern(pattern);

    var renderer = new Renderer();
    try
    {
        using var cts = new CancellationTokenSource();
        Console.CancelKeyPress += (_, e) => { e.Cancel = true; cts.Cancel(); };
        renderer.Animate(grid, generations, delay, cts.Token);
    }
    catch (OperationCanceledException) { }

    Console.WriteLine($"\nStopped at generation {grid.Generation} with {grid.Population} cells.");
}

void LoadCommand(string[] loadArgs)
{
    if (loadArgs.Length == 0)
    {
        Console.Error.WriteLine("Usage: load <file.rle> [--delay <ms>] [--generations <n>]");
        return;
    }

    string filePath = loadArgs[0];
    int delay = 100;
    int generations = int.MaxValue;

    for (int i = 1; i < loadArgs.Length; i++)
    {
        if ((loadArgs[i] == "--delay" || loadArgs[i] == "-d") && i + 1 < loadArgs.Length)
            delay = int.Parse(loadArgs[++i]);
        else if ((loadArgs[i] == "--generations" || loadArgs[i] == "-g") && i + 1 < loadArgs.Length)
            generations = int.Parse(loadArgs[++i]);
    }

    if (!File.Exists(filePath))
    {
        Console.Error.WriteLine($"File not found: {filePath}");
        return;
    }

    var cells = RleParser.ParseFile(filePath);
    var grid = new Grid();
    grid.AddPattern(cells);

    Console.WriteLine($"Loaded {cells.Count} cells from {filePath}");

    var renderer = new Renderer();
    try
    {
        using var cts = new CancellationTokenSource();
        Console.CancelKeyPress += (_, e) => { e.Cancel = true; cts.Cancel(); };
        renderer.Animate(grid, generations, delay, cts.Token);
    }
    catch (OperationCanceledException) { }

    Console.WriteLine($"\nStopped at generation {grid.Generation} with {grid.Population} cells.");
}

void ExportCommand(string[] exportArgs)
{
    string patternName = exportArgs.Length > 0 ? exportArgs[0] : "glider";
    string? outputFile = null;

    for (int i = 1; i < exportArgs.Length; i++)
    {
        if ((exportArgs[i] == "--output" || exportArgs[i] == "-o") && i + 1 < exportArgs.Length)
            outputFile = exportArgs[++i];
    }

    var pattern = Patterns.GetByName(patternName);
    if (pattern == null)
    {
        Console.Error.WriteLine($"Unknown pattern: {patternName}");
        return;
    }

    string rle = RleParser.Export(pattern);

    if (outputFile != null)
    {
        File.WriteAllText(outputFile, rle);
        Console.WriteLine($"Exported {patternName} to {outputFile}");
    }
    else
    {
        Console.Write(rle);
    }
}

void StepCommand(string[] stepArgs)
{
    string patternName = stepArgs.Length > 0 ? stepArgs[0] : "glider";
    int steps = 1;

    for (int i = 1; i < stepArgs.Length; i++)
    {
        if ((stepArgs[i] == "--steps" || stepArgs[i] == "-n") && i + 1 < stepArgs.Length)
            steps = int.Parse(stepArgs[++i]);
    }

    var pattern = Patterns.GetByName(patternName);
    if (pattern == null)
    {
        Console.Error.WriteLine($"Unknown pattern: {patternName}");
        return;
    }

    var grid = new Grid();
    grid.AddPattern(pattern);

    Console.WriteLine($"Initial: Generation {grid.Generation}, Population {grid.Population}");
    grid.Step(steps);
    Console.WriteLine($"After {steps} step(s): Generation {grid.Generation}, Population {grid.Population}");

    // Print the resulting RLE
    Console.WriteLine();
    Console.Write(RleParser.Export(grid));
}

void ListPatterns()
{
    Console.WriteLine("Available patterns:");
    foreach (var name in Patterns.AllNames)
    {
        var p = Patterns.GetByName(name);
        Console.WriteLine($"  {name,-20} ({p?.Count ?? 0} cells)");
    }
}

void PrintUsage()
{
    Console.WriteLine("Conway's Game of Life - CLI");
    Console.WriteLine();
    Console.WriteLine("Usage:");
    Console.WriteLine("  gameoflife run <pattern> [--delay <ms>] [--generations <n>]");
    Console.WriteLine("  gameoflife load <file.rle> [--delay <ms>] [--generations <n>]");
    Console.WriteLine("  gameoflife export <pattern> [-o <file.rle>]");
    Console.WriteLine("  gameoflife step <pattern> [-n <steps>]");
    Console.WriteLine("  gameoflife patterns");
    Console.WriteLine();
    Console.WriteLine("Patterns: " + string.Join(", ", Patterns.AllNames));
}
