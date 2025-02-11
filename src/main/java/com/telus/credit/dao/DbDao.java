package com.telus.credit.dao;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

@Repository
public class DbDao extends AbstractDao {

   private static final String SELECT_NOW = "select now();";

   public DbDao(DataSource dataSource) {
      super(dataSource);
   }

   public String getCurrentDateTime() {
      return getJdbcTemplate().queryForObject(SELECT_NOW, String.class);
   }

}
