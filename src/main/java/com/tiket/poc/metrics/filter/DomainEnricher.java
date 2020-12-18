package com.tiket.poc.metrics.filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
public class DomainEnricher {
  ConcurrentMap<String, Object> domainMetas;

  DomainEnricher(ConcurrentMap<String, Object> domainMetas) {
    this.domainMetas = domainMetas;
  }

  public static DomainEnricher empty() {
    return new DomainEnricher(new ConcurrentHashMap<>());
  }

  public Object computeIfAbsent(String key,
      Function<? super String, ?> mappingFunction) {
    return domainMetas.computeIfAbsent(key, mappingFunction);
  }

  public Object computeIfPresent(String key,
      BiFunction<? super String, ? super Object, ?> remappingFunction) {
    return domainMetas.computeIfPresent(key, remappingFunction);
  }

  public Object compute(String key,
      BiFunction<? super String, ? super Object, ?> remappingFunction) {
    return domainMetas.compute(key, remappingFunction);
  }

  public boolean isEmpty() {
    return domainMetas.isEmpty();
  }

  public boolean containsKey(Object key) {
    return domainMetas.containsKey(key);
  }

  public Object get(Object key) {
    return domainMetas.get(key);
  }

  public Object put(String key, Object value) {
    return domainMetas.put(key, value);
  }

  public Object remove(Object key) {
    return domainMetas.remove(key);
  }
}
