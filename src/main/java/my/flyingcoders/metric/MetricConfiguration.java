package my.flyingcoders.metric;

import com.codahale.metrics.*;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.util.ArrayList;
import java.util.List;

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
    private final MetricRegistry registry;
    private boolean initialized;
    private static MetricConfiguration instance = null;

    protected MetricConfiguration() {
        this.initialized = false;
        this.registry = new MetricRegistry();
    }

    public static MetricConfiguration getInstance() {
        if (instance == null) {
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

    public MetricRegistry metricRegistry(){
        return this.registry;
    }

    public MetricBuilder build(String path){
        if(initialized) {
            throw new IllegalStateException("Already initialized.");
        }

        this.path = path;

        initiateRegistry();

        scanAllTheClassInThePath(path);

        JmxReporter reporter = JmxReporter.forRegistry(this.registry).build();
        reporter.start();
//        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build();
//        reporter.start(10, TimeUnit.SECONDS);
        initialized = true;
        return new MetricBuilder(this);
    }

    private void initiateRegistry() {
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
