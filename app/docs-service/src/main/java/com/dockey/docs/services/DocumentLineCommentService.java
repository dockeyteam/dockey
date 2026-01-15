package com.dockey.docs.services;

import com.dockey.docs.entities.DocumentLineComment;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DocumentLineCommentService {

    private static final Logger LOG = LogManager.getLogger(DocumentLineCommentService.class.getName());

    @PersistenceContext(unitName = "docs-jpa-unit")
    private EntityManager em;

    @PersistenceUnit(unitName = "docs-jpa-unit")
    private EntityManagerFactory emf;

    /**
     * Update or create a line comment count entry using a NEW EntityManager.
     * This method manages its own transaction and is safe to call from background threads.
     */
    public void updateLineCommentCountAsync(Long documentId, Integer lineNumber, Integer newCount) {
        EntityManager localEm = emf.createEntityManager();
        EntityTransaction tx = localEm.getTransaction();
        try {
            tx.begin();
            
            // Use native SQL for guaranteed fresh execution
            int updated = localEm.createNativeQuery(
                "UPDATE document_line_comments SET comment_count = ? " +
                "WHERE document_id = ? AND line_number = ?")
                .setParameter(1, newCount)
                .setParameter(2, documentId)
                .setParameter(3, lineNumber)
                .executeUpdate();
            
            if (updated == 0) {
                // No existing entry, insert new one
                localEm.createNativeQuery(
                    "INSERT INTO document_line_comments (document_id, line_number, comment_count) VALUES (?, ?, ?)")
                    .setParameter(1, documentId)
                    .setParameter(2, lineNumber)
                    .setParameter(3, newCount)
                    .executeUpdate();
                LOG.info("Created line comment entry: docId={} line={} count={}", documentId, lineNumber, newCount);
            } else {
                LOG.info("Updated line comment count: docId={} line={} count={}", documentId, lineNumber, newCount);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOG.error("Failed to update line comment count for docId={} line={}", documentId, lineNumber, e);
            throw e;
        } finally {
            localEm.close();
        }
    }

    /**
     * Delete a line comment count entry using a NEW EntityManager.
     * This method manages its own transaction and is safe to call from background threads.
     */
    public void deleteLineCommentCountAsync(Long documentId, Integer lineNumber) {
        EntityManager localEm = emf.createEntityManager();
        EntityTransaction tx = localEm.getTransaction();
        try {
            tx.begin();
            
            // Use native SQL for guaranteed fresh execution
            int deleted = localEm.createNativeQuery(
                "DELETE FROM document_line_comments WHERE document_id = ? AND line_number = ?")
                .setParameter(1, documentId)
                .setParameter(2, lineNumber)
                .executeUpdate();
            
            if (deleted > 0) {
                LOG.info("Deleted line comment entry: docId={} line={}", documentId, lineNumber);
            } else {
                LOG.info("No line comment entry to delete: docId={} line={}", documentId, lineNumber);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOG.error("Failed to delete line comment count for docId={} line={}", documentId, lineNumber, e);
            throw e;
        } finally {
            localEm.close();
        }
    }

    /**
     * Update or create a line comment count entry.
     * Note: This method should be called within an active transaction context.
     */
    public void updateLineCommentCount(Long documentId, Integer lineNumber, Integer newCount) {
        try {
            // Use a direct update query to avoid detached entity issues
            int updated = em.createQuery(
                "UPDATE DocumentLineComment d SET d.commentCount = :count " +
                "WHERE d.documentId = :docId AND d.lineNumber = :line")
                .setParameter("count", newCount)
                .setParameter("docId", documentId)
                .setParameter("line", lineNumber)
                .executeUpdate();
            
            if (updated == 0) {
                // No existing entry, create new one
                DocumentLineComment lineComment = new DocumentLineComment(documentId, lineNumber, newCount);
                em.persist(lineComment);
                LOG.info("Created line comment entry: docId={} line={} count={}", documentId, lineNumber, newCount);
            } else {
                LOG.info("Updated line comment count: docId={} line={} count={}", documentId, lineNumber, newCount);
            }
            em.flush();
        } catch (Exception e) {
            LOG.error("Failed to update line comment count for docId={} line={}", documentId, lineNumber, e);
            throw e;
        }
    }

    /**
     * Delete a line comment count entry.
     * Note: This method should be called within an active transaction context.
     */
    public void deleteLineCommentCount(Long documentId, Integer lineNumber) {
        try {
            // Use a direct delete query to avoid detached entity issues
            int deleted = em.createQuery(
                "DELETE FROM DocumentLineComment d " +
                "WHERE d.documentId = :docId AND d.lineNumber = :line")
                .setParameter("docId", documentId)
                .setParameter("line", lineNumber)
                .executeUpdate();
            
            if (deleted > 0) {
                LOG.info("Deleted line comment entry: docId={} line={}", documentId, lineNumber);
            } else {
                LOG.info("No line comment entry to delete: docId={} line={}", documentId, lineNumber);
            }
            em.flush();
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
        // Bypass first-level cache to get fresh data from database
        return em.createNamedQuery("DocumentLineComment.findByDocumentId", DocumentLineComment.class)
            .setParameter("documentId", documentId)
            .setHint("javax.persistence.cache.retrieveMode", javax.persistence.CacheRetrieveMode.BYPASS)
            .setHint("javax.persistence.cache.storeMode", javax.persistence.CacheStoreMode.REFRESH)
            .getResultList();
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> getLineCommentCountsMap(Long documentId) {
        // Use native SQL query to completely bypass JPA caching
        // This ensures we always get fresh data from the database
        List<Object[]> results = em.createNativeQuery(
            "SELECT line_number, comment_count FROM document_line_comments WHERE document_id = ?")
            .setParameter(1, documentId)
            .getResultList();
        
        Map<Integer, Integer> countsMap = new HashMap<>();
        for (Object[] row : results) {
            Integer lineNumber = ((Number) row[0]).intValue();
            Integer commentCount = ((Number) row[1]).intValue();
            countsMap.put(lineNumber, commentCount);
        }
        
        LOG.info("Retrieved line comment counts for docId={}: {}", documentId, countsMap);
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
