using Amazon.S3;
using dotnet_app;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.Extensions.Options;
using Npgsql;

var builder = WebApplication.CreateSlimBuilder(args);

// Load app config from yaml file.
builder.Configuration.AddYamlFile("config.yaml", optional: false);

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
static Ok<Device[]> getDevices()
{
    return TypedResults.Ok(Device.GetDevices());
}

// getImage downloads image from S3
static async Task<IResult> getImage(HttpRequest request, IAmazonS3 sess, NpgsqlConnection dbpool, IOptions<Config> options)
{
    // Download the image from S3.
    var _ = await Images.Download(sess, options.Value.s3!.Bucket, "thumbnail.png");

    // Generate a new image.
    var image = Image.NewImage();
    // Save the image ID and the last modified date to the database.
    await Images.Save(image, "go_image", dbpool);

    return Results.Ok(new { Message = "saved" });
}

// getHealth responds with a HTTP 200 or 5xx on error.
static IResult getHealth()
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

