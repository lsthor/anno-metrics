package my.flyingcoders.metric;

import com.codahale.metrics.*;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
TODO : add flag to prevent multiple init
TODO : clean up code
TODO : allow different reporter
TODO : think think
 */
public class MetricConfiguration {
    private String path;
    private boolean memoryUsageGaugeOn;
    private boolean garbageCollectorMetricOn;
    private boolean classLoadingGaugeOn;
    private MetricRegistry registry;
    private static MetricConfiguration instance = null;

    protected MetricConfiguration() {
        this.registry = new MetricRegistry();
    }

    // Lazy Initialization (If required then only)
    public static MetricConfiguration getInstance() {
        if (instance == null) {
            // Thread Safe. Might be costly operation in some case
            synchronized (MetricConfiguration.class) {
                if (instance == null) {
                    instance = new MetricConfiguration();
                }
            }
        }
        return instance;
    }

    public MetricConfiguration withMemoryUsageGaugeOn() {
        this.memoryUsageGaugeOn = true;
        return this;
    }

    public MetricConfiguration withGarbageCollectorMetricOn() {
        this.garbageCollectorMetricOn = true;
        return this;
    }

    public MetricConfiguration withClassLoadingGaugeOn() {
        this.classLoadingGaugeOn = true;
        return this;
    }

    public MetricConfiguration build(String path){
        this.path = path;

        createRegistry();

        scanAllTheClassInThePath(path);

        JmxReporter reporter = JmxReporter.forRegistry(this.registry).build();
        reporter.start();
//        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build();
//        reporter.start(10, TimeUnit.SECONDS);

        return this;
    }

    private void createRegistry() {
        if(memoryUsageGaugeOn) {
            registry.registerAll(new MemoryUsageGaugeSet());
        }
        if(garbageCollectorMetricOn) {
            registry.registerAll(new GarbageCollectorMetricSet());
        }
        if(classLoadingGaugeOn) {
            registry.registerAll(new ClassLoadingGaugeSet());
        }
    }

    public MetricRegistry metricRegistry() {
        return this.registry;
    }

    private void checkIfMethodIsAnnotated() {

    }

    private void scanAllTheClassInThePath(String path) {
        final List<String> matchingMethodNames = new ArrayList<>();
        new FastClasspathScanner(path)
                .matchClassesWithMethodAnnotation(MetricTracking.class, (matchingClass, matchingMethod) -> {
                    MetricTracking metricTracking = matchingMethod.getAnnotation(MetricTracking.class);
                    String metricName = MetricRegistry.name(matchingClass, metricTracking.name());

                    if(metricTracking.meter()) {
                        registry.meter(metricName + "-meter");
                    }
                    if(metricTracking.timer()) {
                        registry.timer(metricName + "-timer");
                    }

                    matchingMethodNames.add(matchingMethod.getName());
                }).scan();
        matchingMethodNames.forEach(System.out::println);
    }

    private void initiateMetricRegistry() {

    }


    public Meter meter(Class clazz, String name) {
        return registry.getMeters().get(MetricRegistry.name(clazz, name) + "-meter");
    }

    public Timer.Context timer(Class clazz, String name) {
        return registry.getTimers().get(MetricRegistry.name(clazz, name) + "-timer").time();
    }

}
