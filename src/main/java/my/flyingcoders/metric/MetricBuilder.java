package my.flyingcoders.metric;

import com.codahale.metrics.*;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MetricBuilder {

    private final MetricConfiguration configuration;

    public MetricBuilder(MetricConfiguration configuration) {
        this.configuration = configuration;
    }

    public MetricBuilder enableJMXReporter() {
        JmxReporter.forRegistry(configuration.metricRegistry()).build().start();
        return this;
    }

    public MetricBuilder enableConsoleReporter(TimeUnit rateUnit, TimeUnit durationUnit, int reportingInterval, TimeUnit reportingUnit){
        ConsoleReporter.forRegistry(configuration.metricRegistry())
                .convertRatesTo(rateUnit)
                .convertDurationsTo(durationUnit)
                .build().start(reportingInterval, reportingUnit);
        return this;
    }

    public MetricBuilder enableSLF4JReporter(String name, TimeUnit rateUnit, TimeUnit durationUnit, int reportingInterval, TimeUnit reportingUnit){
        Slf4jReporter.forRegistry(configuration.metricRegistry())
                .outputTo(LoggerFactory.getLogger(name))
                .convertRatesTo(rateUnit)
                .convertDurationsTo(durationUnit)
                .build().start(reportingInterval, reportingUnit);
        return this;
    }
}
