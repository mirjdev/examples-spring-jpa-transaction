package com.mirjdev.examplesspring.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class ProcedureService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public String generateString(int length) {
        Query query = em.createNativeQuery("SELECT random_string(:length)");
        query.setParameter("length", length);
        String result = (String) query.getSingleResult();
        log.info("generateString result: {}", result);
        return result;
    }
}
