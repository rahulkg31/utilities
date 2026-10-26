package com.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/** Plain JDBC repository for orders stored in PostgreSQL. */
public class JdbcRepository {

    public record Order(long id, String customer, BigDecimal amount) {}

    private final DataSource dataSource;

    public JdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createSchema() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id BIGSERIAL PRIMARY KEY,
                        customer VARCHAR(100) NOT NULL,
                        amount NUMERIC(12, 2) NOT NULL
                    )""");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create schema", e);
        }
    }

    /** Inserts an order and returns the generated id. */
    public long save(String customer, BigDecimal amount) {
        String sql = "INSERT INTO orders (customer, amount) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer);
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save order", e);
        }
    }

    public Optional<Order> findById(long id) {
        String sql = "SELECT id, customer, amount FROM orders WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find order", e);
        }
    }

    public List<Order> findByCustomer(String customer) {
        String sql = "SELECT id, customer, amount FROM orders WHERE customer = ? ORDER BY id";
        List<Order> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query orders", e);
        }
        return result;
    }

    public boolean deleteById(long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete order", e);
        }
    }

    private static Order map(ResultSet rs) throws SQLException {
        return new Order(rs.getLong("id"), rs.getString("customer"), rs.getBigDecimal("amount"));
    }
}
