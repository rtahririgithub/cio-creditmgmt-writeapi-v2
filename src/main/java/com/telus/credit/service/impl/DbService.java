package com.telus.credit.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.telus.credit.dao.DbDao;

@Service
public class DbService {
    @Autowired
    private DbDao repo;

    public String getDateTime() {
        return repo.getCurrentDateTime();
    }
}
