package com.telus.credit.pds.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Reference data item. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"sequence", "key", "values"})
public class SingleKeyReferenceDataItem {

  /** Sequence or priority number for reference data item. (Required) */
  @JsonProperty("sequence")
  @JsonPropertyDescription("Sequence or priority number for reference data item.")
  private Integer sequence;
  /** Key or Code for reference data item. (Required) */
  @JsonProperty("key")
  @JsonPropertyDescription("Key or Code for reference data item.")
  private String key;
  /** English language value for reference data item. (Required) */
  @JsonProperty("values")
  @JsonPropertyDescription("English language value for reference data item.")
  private List<Value> values = null;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /** Sequence or priority number for reference data item. (Required) */
  @JsonProperty("sequence")
  public Integer getSequence() {
    return sequence;
  }

  /** Sequence or priority number for reference data item. (Required) */
  @JsonProperty("sequence")
  public void setSequence(Integer sequence) {
    this.sequence = sequence;
  }

  /** Key or Code for reference data item. (Required) */
  @JsonProperty("key")
  public String getKey() {
    return key;
  }

  /** Key or Code for reference data item. (Required) */
  @JsonProperty("key")
  public void setKey(String key) {
    this.key = key;
  }

  /** English language value for reference data item. (Required) */
  @JsonProperty("values")
  public List<Value> getValues() {
    return values;
  }

  /** English language value for reference data item. (Required) */
  @JsonProperty("values")
  public void setValues(List<Value> values) {
    this.values = values;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SingleKeyReferenceDataItem that = (SingleKeyReferenceDataItem) o;
    return Objects.equals(sequence, that.sequence)
        && Objects.equals(key, that.key)
        && values != null
        && that.getValues() != null
        && values.size() == that.getValues().size()
        && values.containsAll(that.getValues());
  }

  @Override
  public int hashCode() {
    return Objects.hash(sequence, key, values);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
            .append("sequence", sequence)
            .append("key", key)
            .append("values", values)
            .append("additionalProperties", additionalProperties)
            .toString();
  }
}
