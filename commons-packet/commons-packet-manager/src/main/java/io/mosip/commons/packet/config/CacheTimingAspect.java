package io.mosip.commons.packet.config;

import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.core.logger.spi.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class CacheTimingAspect {

    private static final Logger LOGGER = PacketManagerLogger.getLogger(CacheTimingAspect.class);

    @Around("@annotation(cacheable)")
    public Object measureCacheTime(ProceedingJoinPoint pjp, Cacheable cacheable) throws Throwable {
        String methodName = pjp.getSignature().toShortString();
        String[] cacheNames = cacheable.value();
        String args = Arrays.toString(pjp.getArgs());

        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                methodName, "CacheTimingAspect | ENTER | method=" + methodName
                        + " | caches=" + Arrays.toString(cacheNames) + " | args=" + args);

        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                    methodName,
                    "CacheTimingAspect | EXIT | method=" + methodName
                            + " | caches=" + Arrays.toString(cacheNames)
                            + " | args=" + args
                            + " | elapsed=" + elapsedMs + "ms");
        }
    }
}
