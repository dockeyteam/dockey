package com.dockey.checker.services;


import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@ApplicationScoped
public class CheckingService {
        
    private static final Logger LOG = LogManager.getLogger(CheckingService.class.getName());

    public void checkDocument(Long documentId) {
        LOG.info("Checking document with id: {}", documentId);
        return true; //placeholder
    }


    public void checkComment(Long commentId) {
        LOG.info("Checking comment with id: {}", commentId);
        return true; //placeholder
    }



}