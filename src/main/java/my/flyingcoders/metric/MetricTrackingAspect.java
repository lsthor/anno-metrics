package my.flyingcoders.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


@Aspect
public class MetricTrackingAspect {
    @Around("@annotation(metricTracking) && execution(* *(..))")
    public Object invoke(ProceedingJoinPoint joinPoint, MetricTracking metricTracking) throws Throwable {
        if(metricTracking.meter()) {
            MetricConfiguration.getInstance().meter(joinPoint.getTarget().getClass(), metricTracking.name()).mark();
        }

        Object returnObject = null;
        try (TimerContextClosable closable = metricTracking.meter() ? new TimerContextClosable(MetricConfiguration.getInstance().timer(joinPoint.getTarget().getClass(), metricTracking.name())) : null){
            returnObject = joinPoint.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        }
        return returnObject;
    }
}
