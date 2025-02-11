package ${basePackage};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public final class SimpleInsert {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInsert.class);

    private String sql;
    private MapSqlParameterSource parameters;

    public SimpleInsert(String sql, MapSqlParameterSource parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public SimpleInsert setParam(String paramName, Object value) {
        parameters.addValue(paramName, value);
        return this;
    }

    public KeyHolder execute(NamedParameterJdbcDaoSupport jdbcDaoSupport) {
        LOGGER.trace(this.sql);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (jdbcDaoSupport.getNamedParameterJdbcTemplate().update(this.sql, this.parameters, keyHolder) != 1) {
            throw new IncorrectResultSizeDataAccessException(1);
        }
        return keyHolder;
    }
}