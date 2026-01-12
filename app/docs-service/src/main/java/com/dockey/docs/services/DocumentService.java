package com.dockey.docs.services;

import com.dockey.docs.dto.DocumentMetadataResponse;
import com.dockey.docs.entities.Document;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@ApplicationScoped
public class DocumentService {
    
    private static final Logger LOG = LogManager.getLogger(DocumentService.class.getName());
    
    @PersistenceContext(unitName = "docs-jpa-unit")
    private EntityManager em;
    
    public List<Document> getAllDocuments() {
        LOG.info("Fetching all documents");
        TypedQuery<Document> query = em.createNamedQuery("Document.findAll", Document.class);
        return query.getResultList();
    }

    public List<DocumentMetadataResponse> getAllDocumentsByGroup(Long groupId) {
        LOG.info("Fetching all documents in group id: {}", groupId);
        TypedQuery<Document> query = em.createNamedQuery("Document.findGroup", Document.class);
        query.setParameter("groupId", groupId);
        
        List<Document> documents = query.getResultList();
        return documents.stream()
                .map(DocumentMetadataResponse::new)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public Document getDocument(Long id) {
        LOG.info("Fetching document with id: {}", id);
        return em.find(Document.class, id);
    }
    
    public List<Document> getDocumentsByUserId(Long userId) {
        LOG.info("Fetching documents for user: {}", userId);
        TypedQuery<Document> query = em.createNamedQuery("Document.findByUserId", Document.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    
    public Document createDocument(Document document) {
        LOG.info("Creating new document: {}", document.getTitle());
        em.getTransaction().begin();
        try {
            em.persist(document);
            em.flush();
            em.getTransaction().commit();
            return document;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
    
    public Document updateDocument(Long id, Document updatedDocument) {
        LOG.info("Updating document with id: {}", id);
        em.getTransaction().begin();
        try {
            Document document = em.find(Document.class, id);
            
            if (document != null) {
                document.setTitle(updatedDocument.getTitle());
                document.setContent(updatedDocument.getContent());
                document.setStatus(updatedDocument.getStatus());
                em.merge(document);
                em.getTransaction().commit();
                return document;
            }
            
            em.getTransaction().rollback();
            return null;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
    
    public boolean deleteDocument(Long id) {
        LOG.info("Deleting document with id: {}", id);
        em.getTransaction().begin();
        try {
            Document document = em.find(Document.class, id);
            
            if (document != null) {
                em.remove(document);
                em.getTransaction().commit();
                return true;
            }
            
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
