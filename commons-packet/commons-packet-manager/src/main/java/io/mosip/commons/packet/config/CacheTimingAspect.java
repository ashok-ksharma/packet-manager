package io.mosip.commons.packet.config;

import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.core.logger.spi.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class CacheTimingAspect {

    private static final Logger LOGGER = PacketManagerLogger.getLogger(CacheTimingAspect.class);

    @Autowired
    private CacheManager cacheManager;

    @Around("@annotation(cacheable)")
    public Object measureCacheTime(ProceedingJoinPoint pjp, Cacheable cacheable) throws Throwable {
        String methodName = pjp.getSignature().toShortString();
        String[] cacheNames = cacheable.value();
        String cacheKey = Arrays.toString(pjp.getArgs());

        boolean cacheHit = isCacheHit(cacheNames, cacheKey);

        long start = System.nanoTime();
        Object result = pjp.proceed();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                methodName,
                "Cache " + (cacheHit ? "HIT" : "MISS") + " | method=" + methodName
                        + " | caches=" + Arrays.toString(cacheNames)
                        + " | args=" + cacheKey
                        + " | elapsed=" + elapsedMs + "ms");

        return result;
    }

    private boolean isCacheHit(String[] cacheNames, String key) {
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null && cache.get(key) != null) {
                return true;
            }
        }
        return false;
    }
}
