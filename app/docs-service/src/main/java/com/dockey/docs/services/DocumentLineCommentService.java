package com.dockey.docs.services;

import com.dockey.docs.entities.DocumentLineComment;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DocumentLineCommentService {

    private static final Logger LOG = LogManager.getLogger(DocumentLineCommentService.class.getName());

    @PersistenceContext(unitName = "docs-jpa-unit")
    private EntityManager em;

    @Transactional
    public void updateLineCommentCount(Long documentId, Integer lineNumber, Integer newCount) {
        try {
            DocumentLineComment lineComment = findByDocumentIdAndLine(documentId, lineNumber);
            
            if (lineComment == null) {
                // Create new entry
                lineComment = new DocumentLineComment(documentId, lineNumber, newCount);
                em.persist(lineComment);
                LOG.info("Created line comment entry: docId={} line={} count={}", documentId, lineNumber, newCount);
            } else {
                // Update existing entry
                lineComment.setCommentCount(newCount);
                em.merge(lineComment);
                LOG.info("Updated line comment count: docId={} line={} count={}", documentId, lineNumber, newCount);
            }
        } catch (Exception e) {
            LOG.error("Failed to update line comment count for docId={} line={}", documentId, lineNumber, e);
            throw e;
        }
    }

    @Transactional
    public void deleteLineCommentCount(Long documentId, Integer lineNumber) {
        try {
            DocumentLineComment lineComment = findByDocumentIdAndLine(documentId, lineNumber);
            if (lineComment != null) {
                em.remove(lineComment);
                LOG.info("Deleted line comment entry: docId={} line={}", documentId, lineNumber);
            }
        } catch (Exception e) {
            LOG.error("Failed to delete line comment count for docId={} line={}", documentId, lineNumber, e);
            throw e;
        }
    }

    public DocumentLineComment findByDocumentIdAndLine(Long documentId, Integer lineNumber) {
        try {
            return em.createNamedQuery("DocumentLineComment.findByDocumentIdAndLine", DocumentLineComment.class)
                .setParameter("documentId", documentId)
                .setParameter("lineNumber", lineNumber)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<DocumentLineComment> findByDocumentId(Long documentId) {
        return em.createNamedQuery("DocumentLineComment.findByDocumentId", DocumentLineComment.class)
            .setParameter("documentId", documentId)
            .getResultList();
    }

    public Map<Integer, Integer> getLineCommentCountsMap(Long documentId) {
        List<DocumentLineComment> lineComments = findByDocumentId(documentId);
        Map<Integer, Integer> countsMap = new HashMap<>();
        
        for (DocumentLineComment lineComment : lineComments) {
            countsMap.put(lineComment.getLineNumber(), lineComment.getCommentCount());
        }
        
        return countsMap;
    }

    @Transactional
    public void deleteAllForDocument(Long documentId) {
        try {
            int deleted = em.createNamedQuery("DocumentLineComment.deleteByDocumentId")
                .setParameter("documentId", documentId)
                .executeUpdate();
            LOG.info("Deleted {} line comment entries for docId={}", deleted, documentId);
        } catch (Exception e) {
            LOG.error("Failed to delete line comment entries for docId={}", documentId, e);
            throw e;
        }
    }
}
