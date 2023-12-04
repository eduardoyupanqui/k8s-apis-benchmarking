using Amazon.S3;
using dotnet_app;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.Extensions.Options;
using Npgsql;
using OpenTelemetry.Resources;
using OpenTelemetry.Trace;
using Prometheus;

var builder = WebApplication.CreateSlimBuilder(args);

// Load app config from yaml file.
builder.Configuration.AddYamlFile("config.yaml", optional: false);

var serviceName = "dotnet-app";
builder.Services.AddOpenTelemetry()
    .WithTracing(tcb =>
    {
        tcb
        .AddSource(serviceName)
        .SetResourceBuilder(
            ResourceBuilder.CreateDefault()
                .AddService(serviceName: serviceName))
        .AddAspNetCoreInstrumentation()
        .AddOtlpExporter(options => {
            options.Endpoint = new Uri("http://" + builder.Configuration["otlpEndpoint"]!);
            options.Protocol = OpenTelemetry.Exporter.OtlpExportProtocol.Grpc;
        })
        ;
    });

// Create a new tracer provider with a batch span processor and the given exporter.
builder.Services.AddSingleton(TracerProvider.Default.GetTracer(serviceName));

// Create Prometheus registry
Prometheus.Metrics.SuppressDefaultMetrics();
builder.Services.AddSingleton(sp => {
    return new dotnet_app.Metrics(Prometheus.Metrics.DefaultFactory);
});

// Create Prometheus HTTP server to expose metrics
builder.Services.AddMetricServer(options =>
{
    options.Port = 8081;
});

// Add services to the container.
builder.Services.Configure<Config>(builder.Configuration);

builder.Services.S3Connect(builder.Configuration);
builder.Services.DbConnect(builder.Configuration);

var app = builder.Build();

// Define handler functions for each endpoint.
app.MapGet("/api/devices", getDevices);
app.MapGet("/api/images", getImage);
app.MapGet("/health", getHealth);

// Start the main dotnet HTTP server.
app.Run();

// getDevices responds with the list of all connected devices as JSON.
Ok<Device[]> getDevices(Tracer tracer)
{
    return TypedResults.Ok(Device.GetDevices());
}

// getImage downloads image from S3
async Task<IResult> getImage(HttpRequest request, IAmazonS3 sess, NpgsqlConnection dbpool, dotnet_app.Metrics metrics,Tracer tracer, IOptions<Config> options)
{
    // Create a new ROOT span to record and trace the request.
    using var span = tracer.StartActiveSpan("HTTP GET /api/images");

    // Download the image from S3.
    var _ = await Images.Download(sess, options.Value.s3!.Bucket, "thumbnail.png", metrics, tracer);

    // Generate a new image.
    var image = Image.NewImage();
    // Save the image ID and the last modified date to the database.
    await Images.Save(image, "go_image", dbpool, metrics, tracer);

    return Results.Ok(new { Message = "saved" });
}

// getHealth responds with a HTTP 200 or 5xx on error.
IResult getHealth()
{
    return Results.Ok(new { Status = "up" });
}

public static class MyExtensions
{
    // s3Connect initializes the S3 session.
    public static IServiceCollection S3Connect(this IServiceCollection services, IConfiguration Configuration)
    {
        // Create S3 config.
        var s3Options = Configuration.GetSection("s3").Get<S3Config>()!;

        // Establish a new httpClient with the AWS S3 API.
        services.AddSingleton<IAmazonS3>(sp =>
        {
            var clientConfig = new AmazonS3Config
            {
                AuthenticationRegion = s3Options.Region,
                ServiceURL = s3Options.Endpoint,
                ForcePathStyle = s3Options.PathStyle,
            };
            return new AmazonS3Client(s3Options.User, s3Options.Secret, clientConfig);
        });
        return services;
    }
    // dbConnect creates a connection pool to connect to Postgres.
    public static IServiceCollection DbConnect(this IServiceCollection services, IConfiguration Configuration)
    {
        var dbOptions = Configuration.GetSection("db").Get<DbConfig>()!;
        string url = $"Host={dbOptions.Host};Database={dbOptions.Database};Username={dbOptions.User};Password={dbOptions.Password}";
        // Connect to the Postgres database.
        services.AddSingleton<NpgsqlConnection>((sp) => new NpgsqlConnection(url));
        return services;
    }
}

