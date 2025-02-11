package ${basePackage};

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.springframework.jdbc.core.RowMapper;

public abstract class AbstractRowMapper<E> implements RowMapper<E> {

    private Supplier<E> entityFactory;

    private String tableName;

    private ResultSetMetaData metaData;

    public AbstractRowMapper(Supplier<E> entityFactory, String tableName) {
        this.entityFactory = entityFactory;
        this.tableName = tableName;
    }

    protected abstract boolean map(E entity, ResultSet rs, String columnName, int colIdx) throws SQLException;

    @Override
    public E mapRow(ResultSet resultSet, int rowIdx) throws SQLException {
        E entity = entityFactory.get();

        if (this.metaData == null) {
            this.metaData = resultSet.getMetaData();
        }
        boolean hasData = false;
        for (int i = 1; i <= metaData.getColumnCount(); ++i) {
            if (tableName == null || metaData.getTableName(i).equals(tableName)) {
                hasData = map(entity, resultSet, metaData.getColumnName(i), i) || hasData;
            }
        }

        return hasData ? entity : null;
    }
}
