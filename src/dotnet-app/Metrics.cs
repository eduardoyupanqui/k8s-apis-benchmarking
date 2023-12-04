using Prometheus;

namespace dotnet_app;

public class Metrics
{
    public Summary Duration { get; set; }
    public Metrics(IMetricFactory metricFactory){
        Duration = metricFactory.CreateSummary("dotnetapp_request_duration_seconds", "Duration of the request.", ["op"],
        new SummaryConfiguration
        {
            Objectives = new[]
            {
                new QuantileEpsilonPair(0.9, 0.01),
                new QuantileEpsilonPair(0.99, 0.001),
            }
        });;
    }
}
