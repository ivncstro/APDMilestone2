package com.transylvania.repository;

import com.transylvania.config.JpaUtil;
import com.transylvania.model.AdminUser;
import jakarta.persistence.EntityManager;

public class AdminUserRepository {
    public AdminUser findByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM AdminUser u WHERE u.username = :un", AdminUser.class)
                    .setParameter("un", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }
}