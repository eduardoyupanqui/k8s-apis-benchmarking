namespace dotnet_app;
public class Config
{
    // Port to run the http server.
    public int AppPort { get; set; }

    // OTLP Endpoint to send traces.
	public string OTLPEndpoint { get; set; } = string.Empty;

	// S3 config to connect to a bucket.
	public S3Config? s3  { get; set; }

	// DB config to connect to a database.
	public DbConfig? db  { get; set; }
}
public class S3Config
{
    // Region for the S3 bucket.
    public string Region { get; set; } = string.Empty; 

	// S3 bucket name to store images.
	public string Bucket { get; set; } = string.Empty; 

	// S3 endpoint, since we use Minio we must provide
	// a custom endpoint. It should be a DNS of Minio instance.
	public string Endpoint { get; set; } = string.Empty; 

	// User to access S3 bucket.
	public string User { get; set; } = string.Empty; 

	// Secret to access S3 bucket.
	public string Secret { get; set; } = string.Empty; 

	// Enable path S3 style; we must enable it to use Minio.
	public bool PathStyle { get; set; }
}
public class DbConfig
{
    // User to connect database.
    public string User { get; set; } = string.Empty;
    // Password to connect database.
    public string Password { get; set; } = string.Empty;
    // Host to connect database.
    public string Host { get; set; } = string.Empty;
    // Database to store images.
    public string Database { get; set; } = string.Empty;
}