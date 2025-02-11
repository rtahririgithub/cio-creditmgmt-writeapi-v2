package ${basePackage}.operation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import ${basePackage}.entity.${className}Entity;
import ${basePackage}.SimpleInsert;

public class ${className}Operation {

    public static final String INSERT_STATEMENT =
            "INSERT INTO ${tableName}("
<% columns.findAll{c -> c.insert != false}.eachWithIndex { it, idx ->
println "                + \"${idx == 0 ? '' : ','}${it.column}\""
} %>            + ") VALUES ("
<% columns.findAll{c -> c.insert != false}.eachWithIndex { it, idx ->
println "                + \"${idx == 0 ? '' : ','}:${it.attr}\""
} %>            + ")";


    public static final String UPDATE_PREFIX = "UPDATE ${tableName} SET ";

    public static final String SELECT_ALL_STATEMENT = "SELECT * from ${tableName} ";

    public static SimpleInsert insert(${className}Entity entity) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
<% columns.findAll{c -> c.insert != false}.each {
    print "            .addValue(\"${it.attr}\", entity.get${it.attr.capitalize()}())"
    if (it.comment != null) println "  // ${it.comment}" else println ''
} %>;
        return new SimpleInsert(INSERT_STATEMENT, parameters);
    }

    public static String selectAll(String whereStatement) {
        return SELECT_ALL_STATEMENT + " WHERE " + whereStatement;
    }

    private ${className}Operation() {
        //Utils
    }
}
