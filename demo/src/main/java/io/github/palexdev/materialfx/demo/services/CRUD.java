package io.github.palexdev.materialfx.demo.services;
import java.sql.SQLException;
import java.util.List;

public interface CRUD<T> {
    int insert(T t) throws SQLException;
    int update(T t) throws SQLException;
    int delete(T t) throws SQLException;
    List<T> showAll() throws SQLException;
}