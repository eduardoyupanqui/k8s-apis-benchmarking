using System.Text.Json.Serialization;

namespace dotnet_app;

/// <summary>
/// Device represents hardware device
/// </summary>
/// <param name="UUID">Universally unique identifier</param>
/// <param name="Mac">Mac address</param>
/// <param name="Firmware">Firmware version</param>
public record Device([property: JsonPropertyName("UUID")]string UUID, string Mac, string Firmware)
{
    //devices returns pseudo connected devices.
    public static Device[] GetDevices()
    {
        return [
            new Device (UUID: "b0e42fe7-31a5-4894-a441-007e5256afea", Mac: "5F-33-CC-1F-43-82", Firmware: "2.1.6"),
            new Device (UUID: "0c3242f5-ae1f-4e0c-a31b-5ec93825b3e7", Mac: "EF-2B-C4-F5-D6-34", Firmware: "2.1.5"),
            new Device (UUID: "b16d0b53-14f1-4c11-8e29-b9fcef167c26", Mac: "62-46-13-B7-B3-A1", Firmware: "3.0.0"),
            new Device (UUID: "51bb1937-e005-4327-a3bd-9f32dcf00db8", Mac: "96-A8-DE-5B-77-14", Firmware: "1.0.1"),
            new Device (UUID: "e0a1d085-dce5-48db-a794-35640113fa67", Mac: "7E-3B-62-A6-09-12", Firmware: "3.5.6")
        ];
    }
};