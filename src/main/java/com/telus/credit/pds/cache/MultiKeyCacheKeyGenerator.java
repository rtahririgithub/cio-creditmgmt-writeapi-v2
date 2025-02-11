package com.telus.credit.pds.cache;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import com.telus.credit.pds.model.Key;

@Component
public class MultiKeyCacheKeyGenerator implements KeyGenerator {
  @Override
  public Object generate(Object o, Method method, Object... objects) {
    String key = Strings.EMPTY;
    for (Object object : objects) {
      if(object instanceof List) {
        List<Key> keys = (List<Key>) object;
        for (Key k : keys) {
          key += k.getKeyName() + "_" + k.getKeyValue() + "_";
        }
      }
      else if(object instanceof String) {
        key += String.valueOf(object) + "_";
      }
    }
    return key;
  }
}
