using dotnet_app;
using Microsoft.Extensions.Options;

var builder = WebApplication.CreateSlimBuilder(args);
builder.Configuration.AddYamlFile("config.yaml", optional: false);
// Add services to the container.
builder.Services.Configure<Config>(
    builder.Configuration);

var app = builder.Build();

app.MapGet("/api/devices", () => {
    return Device.GetDevices();
});

app.MapGet("/health", () => {
    return new { Status = "up" };
});

app.Run();
