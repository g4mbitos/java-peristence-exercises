package ua.procamp.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import ua.procamp.exception.AccountDaoException;
import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;
    private EntityManager em;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {

        try {
            em = getEntityManager();
            em.persist(account);
            close();
        } catch (Exception ex) {
            throw new AccountDaoException(ex.getMessage(), ex);
        }
    }

    @Override
    public Account findById(Long id) {

        return emf.createEntityManager().find(Account.class, id);

    }

    @Override
    public Account findByEmail(String email) {

        try {

            Account account = emf.createEntityManager()
                    .createQuery("select a from Account a where email = '" + email + "'", Account.class).getSingleResult();
            return account;

        } catch (Exception ex) {
            throw new AccountDaoException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<Account> findAll() {

        return emf.createEntityManager().createQuery("select a from Account a", Account.class).getResultList();
    }

    @Override
    public void update(Account account) {

        try {
            em = getEntityManager();
            em.merge(account);
            close();
        } catch (Exception ex) {
            throw new AccountDaoException(ex.getMessage(), ex);
        }

    }

    @Override
    public void remove(Account account) {

        try {
            em = getEntityManager();
            em.remove(em.contains(account) ? account : em.merge(account));
            close();
        } catch (Exception ex) {
            throw new AccountDaoException(ex.getMessage(), ex);
        }
    }


    private EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        return em;
    }

    private void close() {
        em.getTransaction().commit();
        em.close();
    }

}

