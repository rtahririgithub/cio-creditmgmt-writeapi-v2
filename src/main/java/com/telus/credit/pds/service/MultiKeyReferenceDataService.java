package com.telus.credit.pds.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.telus.credit.common.PdsRefConstants;
import com.telus.credit.pds.model.Key;
import com.telus.credit.pds.model.MultiKeyReferenceDataItem;
import com.telus.credit.pds.model.Value;

@Service
public class MultiKeyReferenceDataService {

  public static List<Key> createKeyList(String keyName, String keyValue) {
    List<Key> keys = new ArrayList<>();
    keys.add(createKey(keyName, keyValue));
    return keys;
  }

  public static Key createKey(String keyName, String keyValue) {
    Assert.notNull(keyName, "keyName parameter is null.");
    Assert.notNull(keyName, "keyValue parameter is null.");
    Key k = new Key();
    k.setKeyName(keyName);
    k.setKeyValue(keyValue);
    return k;
  }

  public MultiKeyReferenceDataItem find(List<MultiKeyReferenceDataItem> source, List<Key> keys) {
    Optional<MultiKeyReferenceDataItem> result =
        source.stream().filter(a -> a.getKeys().containsAll(keys)).findFirst();
    MultiKeyReferenceDataItem refpdsData = null;
    if ((!result.isPresent()) && 
       ((keys.get(0).getKeyName().equals(PdsRefConstants.BUREAU_DECISION_CODE)) ||
        (keys.get(0).getKeyName().equals(PdsRefConstants.MESSAGE_KEY))))
    {
    	refpdsData = null;
    }
    else
    {
    	Assert.isTrue(result.isPresent(), "Value not found for keys=" + keys);
    	refpdsData = result.get();
    }
    return refpdsData;
  }

  public MultiKeyReferenceDataItem find(List<MultiKeyReferenceDataItem> source, List<Key> keys, String lang) {
    MultiKeyReferenceDataItem data = find(source, keys);
    List<Value> result = data.getValues().stream()
            .filter(v -> v.getLangCode().equals(lang))
            .collect(Collectors.toList());
    Assert.notEmpty(result, "Value not found for keys=" + keys + ", lang=" + lang);
    data.setValues(result);
    return data;
  }
}
