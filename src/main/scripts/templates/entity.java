package ${basePackage}.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ${className}Entity {

<% columns.each {
if (it.comment != null) println "    // ${it.comment}"
if (it.insert != null) println "    // Ignored on insert"
println "    private ${it.javaType} ${it.attr};"
println ""
} %>

    private Map<String, Object> updateMap = new HashMap<>();

<% columns.each {
println "    public ${it.javaType} get${it.attr.capitalize()}() { return this.${it.attr}; }"
} %>

<% columns.each {
println "    public void set${it.attr.capitalize()}(${it.javaType} value) { this.${it.attr} = value; this.updateMap.put(\"${it.column}\", this.${it.attr});}"
} %>

<% columns.each {
    println "    public ${className}Entity ${it.attr}(${it.javaType} value) { this.set${it.attr.capitalize()}(value); return this;}"
} %>

<% columns.each {
    println "    public ${className}Entity ${it.attr}(${it.javaType} value, boolean includedForUpdate) { return includedForUpdate ? this.${it.attr}(value) : this;}"
} %>

    public Map<String, Object> getUpdateMap() {
        return this.updateMap;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, "updateMap");
    }

    public enum Cols {
<% columns.each {
println "        ${it.column}${it != columns.last() ? ',' : ';'}";
} %>
    }
}
