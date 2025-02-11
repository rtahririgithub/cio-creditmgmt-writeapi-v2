package com.telus.credit.pds.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Reference data value object. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"langCode", "valueCode", "value"})
public class Value {

  /** Language code for reference data value. */
  @JsonProperty("langCode")
  @JsonPropertyDescription("Language code for reference data value.")
  private String langCode = "en";
  /** Purpose or value code for reference data item. (Required) */
  @JsonProperty("valueCode")
  @JsonPropertyDescription("Purpose or value code for reference data item.")
  private String valueCode;
  /** Text value for reference data item. (Required) */
  @JsonProperty("value")
  @JsonPropertyDescription("Text value for reference data item.")
  private String value;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /** Language code for reference data value. */
  @JsonProperty("langCode")
  public String getLangCode() {
    return langCode;
  }

  /** Language code for reference data value. */
  @JsonProperty("langCode")
  public void setLangCode(String langCode) {
    this.langCode = langCode;
  }

  /** Purpose or value code for reference data item. (Required) */
  @JsonProperty("valueCode")
  public String getValueCode() {
    return valueCode;
  }

  /** Purpose or value code for reference data item. (Required) */
  @JsonProperty("valueCode")
  public void setValueCode(String valueCode) {
    this.valueCode = valueCode;
  }

  /** Text value for reference data item. (Required) */
  @JsonProperty("value")
  public String getValue() {
    return value;
  }

  /** Text value for reference data item. (Required) */
  @JsonProperty("value")
  public void setValue(String value) {
    this.value = value;
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
    Value value1 = (Value) o;
    return Objects.equals(langCode, value1.langCode)
        && Objects.equals(valueCode, value1.valueCode)
        && Objects.equals(value, value1.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(langCode, valueCode, value);
  }

  @Override
  public String toString() {
    return "Value{" +
            "langCode='" + langCode + '\'' +
            ", valueCode='" + valueCode + '\'' +
            ", value='" + value + '\'' +
            ", additionalProperties=" + additionalProperties +
            '}';
  }
}
