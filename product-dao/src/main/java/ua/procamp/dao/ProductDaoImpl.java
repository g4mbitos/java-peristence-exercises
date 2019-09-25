package ua.procamp.dao;

import org.apache.commons.lang3.StringUtils;
import ua.procamp.exception.DaoOperationException;
import ua.procamp.model.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDaoImpl implements ProductDao {

    private DataSource dataSource;


    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {

        verifyProduct(product);

        String SQL_INSERT_PRODUCT = "insert into products (name, producer, price, expiration_date, creation_time) values (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();

            statement = conn.prepareStatement(SQL_INSERT_PRODUCT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setString(2, product.getProducer());
            statement.setBigDecimal(3, product.getPrice());
            statement.setDate(4, Date.valueOf(product.getExpirationDate()));
            statement.setTimestamp(5,
                    Timestamp.valueOf(product.getCreationTime() != null ?
                            product.getCreationTime() :
                            LocalDateTime.now()));

            statement.execute();

            //set product's id generated from database
            Long id = null;
            ResultSet rs = statement.getGeneratedKeys();
            if(rs.next())
                id = rs.getLong(1);
            product.setId(id);

        } catch (SQLException ex) {
            throw new DaoOperationException(ex.getMessage());
        } finally {
            close(conn, statement);
        }

    }


    @Override
    public List<Product> findAll() {

        String SQL_SELECT_ALL_PRODUCTS = "select * from products";

        List<Product> products = new ArrayList<>();

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement(SQL_SELECT_ALL_PRODUCTS);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String producer = resultSet.getString("producer");
                BigDecimal price = resultSet.getBigDecimal("price");

                Date date = resultSet.getDate("expiration_date");
                LocalDate expirationDate = null;
                if (date != null)
                    expirationDate = date.toLocalDate();

                Timestamp timestamp = resultSet.getTimestamp("creation_time");
                LocalDateTime creationTime = null;
                if (timestamp != null)
                    creationTime = timestamp.toLocalDateTime();

                products.add(new Product(id, name, producer, price, expirationDate, creationTime));
            }
            return products;

        } catch (SQLException ex) {
            throw new DaoOperationException(ex.getMessage());
        } finally {
            close(conn, statement, resultSet);
        }


    }

    @Override
    public Product findOne(Long id) {

        String SQL_SELECT_PRODUCT = "select * from products where id = ?";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement(SQL_SELECT_PRODUCT);
            statement.setLong(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String producer = resultSet.getString("producer");
                BigDecimal price = resultSet.getBigDecimal("price");
                LocalDate expirationDate = resultSet.getDate("expiration_date").toLocalDate();
                LocalDateTime creationTime = resultSet.getTimestamp("creation_time").toLocalDateTime();

                return new Product(id, name, producer, price, expirationDate, creationTime);
            }
            else throw new DaoOperationException(String.format("Product with id = %d does not exist",id));

        } catch (SQLException ex) {
            throw new DaoOperationException(ex.getMessage());
        } finally {
            close(conn, statement, resultSet);
        }

    }

    @Override
    public void update(Product product) {

        verifyProductId(product);

        checkExist(product);

        verifyProduct(product);

        String SQL_UPDATE_PRODUCT = "update products set name = ?, producer = ?, price = ?, expiration_date = ?, creation_time = ? where id = ?";
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = dataSource.getConnection();

            statement = conn.prepareStatement(SQL_UPDATE_PRODUCT);
            statement.setString(1, product.getName());
            statement.setString(2, product.getProducer());
            statement.setBigDecimal(3, product.getPrice());
            statement.setDate(4, Date.valueOf(product.getExpirationDate()));
            statement.setTimestamp(5,
                    Timestamp.valueOf(product.getCreationTime() != null ?
                            product.getCreationTime() :
                            LocalDateTime.now()));

            statement.setLong(6, product.getId());
            statement.execute();

        } catch (SQLException ex) {
            throw new DaoOperationException(ex.getMessage());
        } finally {
            close(conn, statement);
        }

    }

    @Override
    public void remove(Product product) {

        verifyProductId(product);

        checkExist(product);

        String SQL_DELETE_PRODUCT = "delete from products where id = ?";
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = dataSource.getConnection();

            statement = conn.prepareStatement(SQL_DELETE_PRODUCT);
            statement.setLong(1, product.getId());
            statement.execute();

        } catch (SQLException ex) {
            throw new DaoOperationException(ex.getMessage());
        } finally {
            close(conn, statement);
        }

    }

    private void verifyProduct(Product product) {
                if(StringUtils.isEmpty(product.getName()) ||
                        StringUtils.isEmpty(product.getProducer()) ||
                        product.getPrice() == null ||
                        product.getExpirationDate() == null){
                    throw new DaoOperationException(String.format("Error saving product: %s", product));
                }
    }

    private void verifyProductId(Product product){
        if(product.getId() == null)
            throw new DaoOperationException("Product id cannot be null");
    }

    private void checkExist(Product product){
        if(!exist(product))
            throw new DaoOperationException(String.format("Product with id = %d does not exist",product.getId()));
    }

    private boolean exist(Product product){
        String SQL_SELECT_PRODUCT = "select id from products where id = ?";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement(SQL_SELECT_PRODUCT);
            statement.setLong(1, product.getId());
            resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException ex) {
            throw new DaoOperationException(ex.getMessage());
        } finally {
            close(conn, statement, resultSet);
        }
    }

    private void close(Connection conn, PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ex) {
                throw new DaoOperationException(ex.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                throw new DaoOperationException(ex.getMessage());
            }
        }
    }

    private void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
        close(conn, stmt);
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                throw new DaoOperationException(ex.getMessage());
            }
        }
    }


}
