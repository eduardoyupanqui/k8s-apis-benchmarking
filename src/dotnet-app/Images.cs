using System.Data;
using Amazon.Runtime.Internal;
using Amazon.S3;
using Amazon.S3.Model;
using Npgsql;
using NpgsqlTypes;

namespace dotnet_app;

/// <summary>
/// Image represents the image uploaded by the user.
/// </summary>
/// <param name="ImageUUID">ImageUUID is the unique ID of the image</param>
/// <param name="LastModified">LastModified is the timestamp when the image was last modified.</param>
public record Image(string ImageUUID, DateTime LastModified)
{
    // NewImage creates a new image.
    public static Image NewImage()
    {
        // Generate a new UUID for the image.
        var id = Guid.NewGuid().ToString();

        // Simulate the last modified date.
        var lastModified = DateTime.UtcNow;

        // Create an image with the generated ID and timestamp.
        return new Image(id, lastModified);
    }
}
public class Images
{
    // Save inserts a newly generated image into the Postgres database.
    public static async Task Save(Image image, string table, NpgsqlConnection dbpool)
    {
        // Prepare the database query to insert a record.
        var query = string.Format("INSERT INTO {0} VALUES (:id, :lastModified)", table);

        // Execute the query to create a new image record.
        using var cmd = new NpgsqlCommand(query, dbpool);
        await dbpool.OpenAsync();
        cmd.Parameters.AddWithValue("id", new Guid(image.ImageUUID));
        cmd.Parameters.AddWithValue("lastModified", image.LastModified);
        await cmd.ExecuteNonQueryAsync();
        await dbpool.CloseAsync();
    }

    //download downloads S3 image and returns last modified date.
    public static async Task<DateTime> Download(IAmazonS3 client, string bucketName, string objectName)
    {
        // Prepare the request for the S3 bucket.
        var request = new GetObjectRequest
        {
            BucketName = bucketName,
            Key = objectName,
        };

        // Send the request to the S3 object store to download the image.
        using var response = await client.GetObjectAsync(request);
        if (response.HttpStatusCode != System.Net.HttpStatusCode.OK)
        {
            throw new HttpRequestException($"client.GetObjectAsync failed", null, response.HttpStatusCode);
        }
        
        // Read all the image bytes returned by AWS.
        using var ms = new MemoryStream();
        await response.ResponseStream.CopyToAsync(ms);
        return response.LastModified;
    }
}
