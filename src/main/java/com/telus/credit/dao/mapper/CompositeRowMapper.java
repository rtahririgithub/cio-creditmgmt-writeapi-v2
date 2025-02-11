package com.telus.credit.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

/**
 * Map entities when the sql statement queries from multiple tables
 */
public class CompositeRowMapper implements RowMapper<CompositeRowMapper.CompositeEntity> {

    private List<RowMapper<?>> rowMappers;

    /**
     * Constructor, accept multiple RowMappers by order. When getting the entities with
     * CompositeEntity.getEntity, the entity returned accordingly with the order of RowMappers
     *
     * @param rowMappers
     */
    public CompositeRowMapper(RowMapper<?> ...rowMappers) {
        this.rowMappers = Arrays.asList(rowMappers);
    }

    @Override
    public CompositeEntity mapRow(ResultSet resultSet, int i) throws SQLException {
        CompositeEntity compositeEntity = new CompositeEntity();
        for (RowMapper<?> rowMapper : rowMappers) {
            Object result = rowMapper.mapRow(resultSet, i);
            compositeEntity.addResult(result);
        }
        return compositeEntity;
    }

    public static class CompositeEntity {
        private List<Object> results = new ArrayList<>();

        public void addResult(Object result) {
            this.results.add(result);
        }

        /**
         * get the entities accordingly with the order of RowMappers
         *
         * @param position
         * @param clazz
         * @param <T>
         * @return
         */
        public <T> T getEntity(int position, Class<T> clazz) {
            return (T) results.get(position);
        }
    }
}
