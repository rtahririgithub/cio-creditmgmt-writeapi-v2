package com.telus.credit.pds.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.telus.credit.config.ReferenceDataConfig;
import com.telus.credit.pds.model.Key;
import com.telus.credit.pds.model.MultiKeyReferenceDataItem;
import com.telus.credit.pds.model.ReferenceData;
import com.telus.credit.pds.model.SingleKeyReferenceDataItem;

@Service
public class ReferenceDataService {

  @Autowired SingleKeyReferenceDataService singleKeyService;

  @Autowired MultiKeyReferenceDataService multiKeyService;

  @Autowired ReferenceData referenceData;

  public ReferenceData getReferenceData() {
    return this.referenceData;
  }

  @Cacheable(
      value = ReferenceDataConfig.ADDRESS_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null")
  public SingleKeyReferenceDataItem getAddressType(String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getAddressType(), key, lang, valueCode);
  }

  @Cacheable(
      value = ReferenceDataConfig.JURISDICTION_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null")
  public SingleKeyReferenceDataItem getJurisdictionType(String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getJurisdictionType(), key, lang, valueCode);
  }

  @Cacheable(
      value = ReferenceDataConfig.CDA_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null")
  public SingleKeyReferenceDataItem getCdaBillingAccountType(
      String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getCdaBillingAccountType(), key, lang, valueCode);
  }

  @Cacheable(
      value = ReferenceDataConfig.COUNTRY_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null")
  public SingleKeyReferenceDataItem getCountryOverseas(String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getCountryOverseas(), key, lang, valueCode);
  }

  @Cacheable(
      value = ReferenceDataConfig.PROVINCE_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null")
  public SingleKeyReferenceDataItem getProvinceState(String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getProvinceState(), key, lang, valueCode);
  }

  @Cacheable(
      value = ReferenceDataConfig.CREDIT_CLASS_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null")
  public SingleKeyReferenceDataItem getCreditClass(String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getCreditClass(), key, lang, valueCode);
  }

  @Cacheable(
          value = ReferenceDataConfig.ASSESSMENT_CACHE,
          cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
          unless = "#result == null",
          keyGenerator = "multiKeyCacheKeyGenerator")
  public MultiKeyReferenceDataItem getAssessmentMessage(List<Key> keys) {
    return multiKeyService.find(referenceData.getAssessmentMessage(), keys);
  }

  @Cacheable(
          value = ReferenceDataConfig.ASSESSMENT_CACHE,
          cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
          unless = "#result == null",
          keyGenerator = "multiKeyCacheKeyGenerator")
  public MultiKeyReferenceDataItem getAssessmentMessage(List<Key> keys, String lang) {
    return multiKeyService.find(referenceData.getAssessmentMessage(), keys, lang);
  }

  @Cacheable(
      value = ReferenceDataConfig.CREDIT_DECISION_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null",
      keyGenerator = "multiKeyCacheKeyGenerator")
  public MultiKeyReferenceDataItem getCreditDecisionRule(List<Key> keys) {
    return multiKeyService.find(referenceData.getCreditDecisionRule(), keys);
  }

  @Cacheable(
      value = ReferenceDataConfig.CREDIT_OPERATION_CACHE,
      cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
      unless = "#result == null",
      keyGenerator = "multiKeyCacheKeyGenerator")
  public MultiKeyReferenceDataItem getCreditOperationParameter(List<Key> keys) {
    return multiKeyService.find(referenceData.getCreditOperationParameter(), keys);
  }

  @Cacheable(
          value = ReferenceDataConfig.CREDIT_PROGRAM_CACHE,
          cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
          unless = "#result == null")
  public SingleKeyReferenceDataItem getCreditProgramName(String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getCreditProgramName(), key, lang, valueCode);
  }

  @Cacheable(
          value = ReferenceDataConfig.CREDIT_WARNING_CACHE,
          cacheManager = ReferenceDataConfig.REFERENCE_DATA_CACHE_MANAGER,
          unless = "#result == null")
  public SingleKeyReferenceDataItem getCreditWarningCategory(String key, String lang, String valueCode) {
    return singleKeyService.find(referenceData.getCreditWarningCategory(), key, lang, valueCode);
  }
}
