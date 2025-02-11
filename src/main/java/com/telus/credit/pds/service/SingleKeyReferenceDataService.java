package com.telus.credit.pds.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.telus.credit.pds.model.SingleKeyReferenceDataItem;
import com.telus.credit.pds.model.Value;

@Service
public class SingleKeyReferenceDataService {

  public SingleKeyReferenceDataItem find(List<SingleKeyReferenceDataItem> source, String key) {
    Optional<SingleKeyReferenceDataItem> result =
        source.stream().filter(a -> a.getKey().equals(key)).findFirst();
    Assert.isTrue(result.isPresent(), "Value not found for key=" + key);
    return result.get();
  }

  public SingleKeyReferenceDataItem find(
      List<SingleKeyReferenceDataItem> source, String key, String lang) {
    SingleKeyReferenceDataItem data = find(source, key);
    List<Value> result =
        data.getValues().stream()
            .filter(v -> v.getLangCode().equals(lang))
            .collect(Collectors.toList());
    Assert.notEmpty(result, "Value not found for key=" + key + ", lang=" + lang);
    data.setValues(result);
    return data;
  }

  public SingleKeyReferenceDataItem find(
      List<SingleKeyReferenceDataItem> source, String key, String lang, String valueCode) {
    SingleKeyReferenceDataItem data = find(source, key);
    List<Value> result =
        data.getValues().stream()
            .filter(v -> v.getValueCode().equals(valueCode) && v.getLangCode().equals(lang))
            .collect(Collectors.toList());
    Assert.notEmpty(
        result, "Value not found for key=" + key + ", lang=" + lang + ", valueCode=" + valueCode);
    data.setValues(result);
    return data;
  }
}
