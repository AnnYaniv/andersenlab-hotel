package com.andersenlab.hotel.repository.jdbc;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ApartmentSort;
import com.andersenlab.hotel.model.ApartmentStatus;
import com.andersenlab.hotel.repository.SortableCrudRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcApartmentRepository implements SortableCrudRepository<Apartment, ApartmentSort> {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcApartmentRepository.class);
    private final DataSource source;

    public JdbcApartmentRepository(DataSource source) {
        this.source = source;
    }

    @Override
    public void save(Apartment entity) {
        final String query = """
                INSERT INTO apartment(
                price, capacity, availability, status, id)
                VALUES (?, ?, ?, ?, ?);
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            putObjectToResultSet(statement, entity);
            statement.execute();
        } catch (SQLException e) {
            throw new CouldNotExecuteSql("Save statement was not executed", e);
        }
    }

    @Override
    public Collection<Apartment> findAllSorted(ApartmentSort sort) {
        final List<Apartment> apartments = new ArrayList<>();
        final String query = String.format("""
                SELECT id, price, capacity, availability, status
                FROM apartment
                ORDER BY %s;
                """, sort.name());

        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(query);
            while (set.next()) {
                apartments.add(getObjectFromResultSet(set));
            }
        } catch (SQLException e) {
            throw new CouldNotExecuteSql("Find all statement was not executed ", e);
        }
        return apartments;
    }

    @Override
    public void delete(UUID id) {
        final String query = """
                DELETE FROM apartment
                WHERE id = ?
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
            LOG.info("Query executed. Apartment deleted: {}", id);
        } catch (SQLException e) {
            throw new CouldNotExecuteSql("Delete statement was not executed ", e);
        }
    }

    @Override
    public boolean has(UUID id) {
        final String query = """
                SELECT COUNT(*) as quantity
                FROM APARTMENT
                WHERE id = ?
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("quantity") > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new CouldNotExecuteSql("Failed to check client existence: ", e);
        }
    }

    @Override
    public Optional<Apartment> getById(UUID id) {
        final String query = """
                SELECT id, price, capacity, availability, status
                FROM apartment
                WHERE id = ?
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id.toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(getObjectFromResultSet(set));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new CouldNotExecuteSql("Find all statement was not executed ", e);
        }
    }

    @Override
    public void update(Apartment entity) {
        final String query = """
                UPDATE apartment
                SET price=?, capacity=?, availability=?, status=?
                WHERE id=?;
                """;

        try (Connection connection = source.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            putObjectToResultSet(statement, entity);
            statement.execute();
        } catch (SQLException e) {
            throw new CouldNotExecuteSql("Update statement was not executed {}", e);
        }
    }

    @SneakyThrows
    private void putObjectToResultSet(PreparedStatement statement, Apartment apartment) {
        statement.setBigDecimal(1, apartment.getPrice());
        statement.setInt(2, apartment.getCapacity().intValue());
        statement.setBoolean(3, apartment.isAvailability());
        statement.setString(4, apartment.getStatus().toString());
        statement.setString(5, apartment.getId().toString());
    }

    @SneakyThrows
    private Apartment getObjectFromResultSet(ResultSet resultSet) {
        return new Apartment(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getBigDecimal("price"),
                BigInteger.valueOf(resultSet.getInt("capacity")),
                resultSet.getBoolean("availability"),
                EnumUtils.getEnum(ApartmentStatus.class, resultSet.getString("status"))
        );
    }
}
