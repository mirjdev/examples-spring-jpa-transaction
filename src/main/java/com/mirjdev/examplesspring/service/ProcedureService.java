package com.mirjdev.examplesspring.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
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
        StoredProcedureQuery procedureQuery = em.createStoredProcedureQuery("random_string");
        procedureQuery.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        procedureQuery.setParameter(1, length);
        String result = (String) procedureQuery.getSingleResult();
        log.info("generateString result: {}", result);
        return result;
    }
}
