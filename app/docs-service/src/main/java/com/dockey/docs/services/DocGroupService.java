package com.dockey.docs.services;

import com.dockey.docs.entities.DocGroup;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class DocGroupService {
    
    private static final Logger LOG = LogManager.getLogger(DocGroupService.class.getName());
    
    @PersistenceContext(unitName = "docs-jpa-unit")
    private EntityManager em;
    
    /**
     * Get all document groups ordered by display name
     */
    public List<DocGroup> getAllGroups() {
        LOG.info("Fetching all document groups");
        return em.createNamedQuery("DocGroup.findAll", DocGroup.class)
                .getResultList();
    }
    
    /**
     * Get a specific group by ID
     */
    public DocGroup getGroupById(Long id) {
        LOG.info("Fetching group with id: {}", id);
        return em.find(DocGroup.class, id);
    }
    
    /**
     * Get a specific group by name (slug)
     */
    public DocGroup getGroupByName(String name) {
        LOG.info("Fetching group with name: {}", name);
        try {
            return em.createNamedQuery("DocGroup.findByName", DocGroup.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOG.warn("No group found with name: {}", name);
            return null;
        }
    }
    
    /**
     * Create a new document group
     */
    @Transactional
    public DocGroup createGroup(DocGroup group) {
        LOG.info("Creating new document group: {}", group.getDisplayName());
        
        // Check if group with same name already exists
        DocGroup existing = getGroupByName(group.getName());
        if (existing != null) {
            LOG.warn("Group with name '{}' already exists", group.getName());
            throw new IllegalArgumentException("Group with name '" + group.getName() + "' already exists");
        }
        
        em.persist(group);
        em.flush();
        LOG.info("Created group with id: {}", group.getId());
        return group;
    }
    
    /**
     * Update an existing document group
     */
    @Transactional
    public DocGroup updateGroup(Long id, DocGroup updatedGroup) {
        LOG.info("Updating group with id: {}", id);
        
        DocGroup existingGroup = em.find(DocGroup.class, id);
        if (existingGroup == null) {
            LOG.warn("Group not found with id: {}", id);
            return null;
        }
        
        // Check if name is being changed and if it conflicts with another group
        if (!existingGroup.getName().equals(updatedGroup.getName())) {
            DocGroup conflicting = getGroupByName(updatedGroup.getName());
            if (conflicting != null && !conflicting.getId().equals(id)) {
                LOG.warn("Group with name '{}' already exists", updatedGroup.getName());
                throw new IllegalArgumentException("Group with name '" + updatedGroup.getName() + "' already exists");
            }
        }
        
        // Update fields
        existingGroup.setName(updatedGroup.getName());
        existingGroup.setDisplayName(updatedGroup.getDisplayName());
        existingGroup.setDescription(updatedGroup.getDescription());
        existingGroup.setIcon(updatedGroup.getIcon());
        existingGroup.setTechnology(updatedGroup.getTechnology());
        
        em.merge(existingGroup);
        em.flush();
        LOG.info("Updated group with id: {}", id);
        return existingGroup;
    }
    
    /**
     * Delete a document group
     * Note: Documents referencing this group will have their group_id set to NULL
     */
    @Transactional
    public boolean deleteGroup(Long id) {
        LOG.info("Deleting group with id: {}", id);
        
        DocGroup group = em.find(DocGroup.class, id);
        if (group == null) {
            LOG.warn("Group not found with id: {}", id);
            return false;
        }
        
        em.remove(group);
        em.flush();
        LOG.info("Deleted group with id: {}", id);
        return true;
    }
    
    /**
     * Get count of documents in a group
     */
    public Integer getDocumentCountForGroup(Long groupId) {
        LOG.info("Counting documents for group id: {}", groupId);
        Long count = em.createQuery(
                "SELECT COUNT(d) FROM Document d WHERE d.docGroup.id = :groupId", Long.class)
                .setParameter("groupId", groupId)
                .getSingleResult();
        return count.intValue();
    }
}
