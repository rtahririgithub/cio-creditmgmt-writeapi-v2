package ${basePackage};

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public final class SimpleUpdate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUpdate.class);

    private Map<String, List<Object>> sets = new HashMap<>();
    private String where = "";
    private LinkedList<Object> whereParams = new LinkedList<>();

    public SimpleUpdate(Map<String, Object> updateMap) {
        if (updateMap == null || updateMap.isEmpty()) {
            return;
        }

        updateMap.entrySet().forEach(entry -> {
            sets.put(entry.getKey(), Arrays.asList("?", entry.getValue()));
        });
    }

    public SimpleUpdate set(String field, String expression, Object... args) {
        List<Object> params = new ArrayList<Object>(Arrays.asList(expression));
        for (Object arg : args) {
            params.add(arg);
        }
        sets.put(field, params);

        return this;
    }

    public SimpleUpdate where(String whereExpression, Object... params) {
        where = " WHERE " + whereExpression;
        for (Object param : params) {
            whereParams.add(param);
        }

        return this;
    }

    public int execute(String updatePrefix, JdbcDaoSupport jdbcDaoSupport) {
        if (StringUtils.isBlank(where)) {
            throw new IllegalStateException("Expect WHERE statement");
        }

        StringBuilder sqlBuilder = new StringBuilder(updatePrefix);
        List<Object> params = new LinkedList<>();

        for (Map.Entry<String, List<Object>> update : sets.entrySet()) {
            sqlBuilder.append(update.getKey()).append("=").append(update.getValue().get(0)).append(", ");
            if (update.getValue().size() > 1) {
                update.getValue().stream().skip(1).forEach(v -> params.add(v));
            }
        }
        sqlBuilder.setLength(sqlBuilder.length() - 2);
        params.addAll(whereParams);

        String sql = sqlBuilder.toString() + where;
        LOGGER.trace(sql);

        return jdbcDaoSupport.getJdbcTemplate().update(sql, params.toArray(new Object[0]));
    }
}