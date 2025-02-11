package com.telus.credit.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.pds.model.ReferenceData;

@Configuration
@EnableCaching
public class ReferenceDataConfig {

  public static final String REFERENCE_DATA_FILE_NAME = "ref-pds-data.json";
  public static final String REFERENCE_DATA_CACHE_MANAGER = "referenceDataCacheManager";
  public static final String ADDRESS_CACHE = "ADDRESS_CACHE";
  public static final String CDA_CACHE = "CDA_CACHE";
  public static final String COUNTRY_CACHE = "COUNTRY_CACHE";
  public static final String CREDIT_CLASS_CACHE = "CREDIT_CLASS_CACHE";
  public static final String CREDIT_DECISION_CACHE = "CREDIT_DECISION_CACHE";
  public static final String CREDIT_OPERATION_CACHE = "CREDIT_OPERATION_CACHE";
  public static final String JURISDICTION_CACHE = "JURISDICTION_CACHE";
  public static final String PROVINCE_CACHE = "PROVINCE_CACHE";
  public static final String ASSESSMENT_CACHE = "ASSESSMENT_CACHE";
  public static final String CREDIT_PROGRAM_CACHE = "CREDIT_PROGRAM_CACHE";
  public static final String CREDIT_WARNING_CACHE = "CREDIT_WARNING_CACHE";

  @Bean
  public ReferenceData referenceData() throws IOException {
    InputStream is = null;
    try {
      is = getClass().getClassLoader().getResourceAsStream(REFERENCE_DATA_FILE_NAME);
      ObjectMapper mapper = new ObjectMapper();
      ReferenceData referenceData = mapper.readValue(is, new TypeReference<ReferenceData>() {
      });
      return referenceData;
    }
    finally {
      IOUtils.closeQuietly(is);
    }
  }

  @Bean
  public CacheManager referenceDataCacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(
        Arrays.asList(
            new ConcurrentMapCache(ADDRESS_CACHE),
            new ConcurrentMapCache(CDA_CACHE),
            new ConcurrentMapCache(COUNTRY_CACHE),
            new ConcurrentMapCache(CREDIT_CLASS_CACHE),
            new ConcurrentMapCache(CREDIT_DECISION_CACHE),
            new ConcurrentMapCache(CREDIT_OPERATION_CACHE),
            new ConcurrentMapCache(JURISDICTION_CACHE),
            new ConcurrentMapCache(PROVINCE_CACHE),
            new ConcurrentMapCache(ASSESSMENT_CACHE),
            new ConcurrentMapCache(CREDIT_PROGRAM_CACHE),
            new ConcurrentMapCache(CREDIT_WARNING_CACHE)));
    return cacheManager;
  }
}
