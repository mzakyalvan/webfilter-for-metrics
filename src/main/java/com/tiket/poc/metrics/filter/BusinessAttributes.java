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
public class BusinessAttributes {
  ConcurrentMap<String, Object> domainMetas;

  BusinessAttributes(ConcurrentMap<String, Object> domainMetas) {
    this.domainMetas = domainMetas;
  }

  /**
   * Create new {@link BusinessAttributes} with empty attributes.
   *
   * @return
   */
  public static BusinessAttributes empty() {
    return new BusinessAttributes(new ConcurrentHashMap<>());
  }

  public Object computeIfAbsent(String key, Function<? super String, ?> mapper) {
    return domainMetas.computeIfAbsent(key, mapper);
  }

  public Object computeIfPresent(String key,
      BiFunction<? super String, ? super Object, ?> remapping) {
    return domainMetas.computeIfPresent(key, remapping);
  }

  public Object compute(String key,
      BiFunction<? super String, ? super Object, ?> remapping) {
    return domainMetas.compute(key, remapping);
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
