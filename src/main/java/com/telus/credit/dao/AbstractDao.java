package com.telus.credit.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceUtils;

public abstract class AbstractDao extends NamedParameterJdbcDaoSupport {

    protected static final String NOW = "now()";

    public AbstractDao(DataSource dataSource) {
        this.setDataSource(dataSource);
    }

    /**
     * Wrapper of JdbcTemplate.queryForObject. Instead of returning null, it returns optional
     *
     * @param rowMapper
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public <T> Optional<T> queryForObject(RowMapper<T> rowMapper, String sql, Object... params) {
        try {
            return Optional.of(this.getJdbcTemplate().queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    
    /**
     * Wrapper of JdbcTemplate . Instead of returning null, it returns optional
     * @param <T>
     *
     * @param rowMapper
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public <T> Optional<List<T>> query(RowMapper<T> rowMapper, String sql, Object... params) {
        try {        	
            return  Optional.of(this.getJdbcTemplate().query(sql, params, rowMapper));
         } catch (EmptyResultDataAccessException e) {
             return Optional.empty();
         }
    }
        
    
    /**
     * Update entity based on its update map.
     *
     * @param updateMap A map of columns and values
     * @param id ID value, this value will be used in WHERE statement
     * @param idColumn column name used for ID. This column will be used for WHERE statement
     * @param versionColumn column name used for version
     * @return SimpleUpdate object with WHERE statement set for columnId and version
     */
    protected SimpleUpdate updateWithVersioning(Map<String, Object> updateMap, Object id, String idColumn, String versionColumn) {
        if (updateMap == null || updateMap.isEmpty()) {
            return null;
        }

        updateMap.remove(idColumn);
        Object currVersion = updateMap.remove(versionColumn);

        SimpleUpdate update = new SimpleUpdate(updateMap).set(versionColumn, versionColumn + "+1");
        if (NumberUtils.isParsable(Objects.toString(currVersion))) {
            update.where(idColumn + "=? AND " + versionColumn + "=?", id, currVersion);
        } else {
            throw new IllegalArgumentException("Current version not found or invalid " + currVersion);
        }

       return update;
    }

    
    public void reconnectToDatabase() {
        Connection connection = DataSourceUtils.getConnection(super.getDataSource());
        try {
            if (connection != null && !connection.isValid(2)) {
                DataSourceUtils.releaseConnection(connection, super.getDataSource());
                connection = DataSourceUtils.getConnection(super.getDataSource()); // Obtain a new connection
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reconnect to the database", e);
        } finally {
        	//resource cleanup
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, super.getDataSource());
            }        	
        }
    }       
}
