var builder = WebApplication.CreateSlimBuilder(args);

// Add services to the container.

var app = builder.Build();

app.MapGet("/ping", () =>
{
    return new { Message = "pong" };
});

app.Run();
