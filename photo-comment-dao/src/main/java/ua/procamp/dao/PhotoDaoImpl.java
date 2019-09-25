package ua.procamp.dao;

import ua.procamp.model.Photo;
import ua.procamp.model.PhotoComment;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {

        try {
            entityManager = getEntityManager();
            entityManager.persist(photo);
            close();
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Impossible to save %s", photo), ex);
        }
    }

    @Override
    public Photo findById(long id) {
        return entityManagerFactory.createEntityManager().find(Photo.class, id);
    }

    @Override
    public List<Photo> findAll() {
        return entityManagerFactory.createEntityManager().createQuery("select p from Photo p").getResultList();
    }

    @Override
    public void remove(Photo photo) {
        try {
            entityManager = getEntityManager();
            entityManager.remove(entityManager.contains(photo) ? photo : entityManager.merge(photo));
            close();
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Impossible to remove %s", photo), ex);
        }
    }

    @Override
    public void addComment(long photoId, String comment) {
        Photo photo = findById(photoId);
        if (photo == null)
            throw new RuntimeException(String.format("not found photo with id %d", photoId));
        try {
            entityManager = getEntityManager();

            PhotoComment photoComment = new PhotoComment();
            photoComment.setText(comment);
            photoComment.setCreatedOn(LocalDateTime.now());

            photo.addComment(photoComment);
            entityManager.merge(photo);

            close();
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Impossible to add comment %s",comment));
        }
    }

    private EntityManager getEntityManager() {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        return em;
    }

    private void close() {
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
