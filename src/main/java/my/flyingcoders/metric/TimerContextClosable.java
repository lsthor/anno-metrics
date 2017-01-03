package my.flyingcoders.metric;

import com.codahale.metrics.Timer;

/**
 * Created by thor on 29/12/2016.
 */
public class TimerContextClosable implements AutoCloseable {
    private final Timer.Context context;

    public TimerContextClosable(Timer.Context context) {
        this.context = context;
    }

    @Override
    public void close() throws Exception {
        context.close();
    }
}
