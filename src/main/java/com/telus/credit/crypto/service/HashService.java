package com.telus.credit.crypto.service;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.hash.Hashing;

@Service
public class HashService {

  public String sha512(String data) {
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input data=" + data);
    return Hashing.sha512().hashString(data, StandardCharsets.UTF_8).toString();
  }

  public String sha512CaseInsensitive(String data) {
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input data=" + data);
    return Hashing.sha512().hashString(data.toUpperCase(), StandardCharsets.UTF_8).toString();
  }

  public String hmacSha512(String key, String data) {
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input key=" + key);
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input data=" + data);
    return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8))
        .hashString(data, StandardCharsets.UTF_8)
        .toString();
  }

  public String hmacMd5(String key, String data) {
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input key=" + key);
    Assert.isTrue(StringUtils.isNotBlank(data), "Invalid input data=" + data);
    return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8))
        .hashString(data, StandardCharsets.UTF_8)
        .toString();
  }
}
