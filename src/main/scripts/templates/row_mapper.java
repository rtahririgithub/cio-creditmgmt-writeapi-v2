package ${basePackage}.rowmapper;

import ${basePackage}.AbstractRowMapper;
import ${basePackage}.entity.${className}Entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public class ${className}RowMapper extends AbstractRowMapper<${className}Entity> {
    public ${className}RowMapper(Supplier<${className}Entity> entityFactory) {
        super(entityFactory, "<% print tableName %>");
    }

    public ${className}RowMapper() {
        this(${className}Entity::new);
    }

    @Override
    protected boolean map(${className}Entity e, ResultSet rs, String columnName, int colIdx)  throws SQLException {
        switch(columnName) {
<% columns.each {
println "            case \"${it.column}\":"
if (it.mappingType != null) {
println "                e.set${it.attr.capitalize()}(rs.get${it.mappingType}(colIdx));"
} else{
println"                e.set${it.attr.capitalize()}((${it.javaType})rs.getObject(colIdx));"
}

println "                break;"
} %>
            default: return false;
        }

        return true;
    }
}
