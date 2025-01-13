package org.iesvdm.ventas_sb;

import lombok.extern.slf4j.Slf4j;
import org.iesvdm.ventas_sb.modelo.Cliente;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JDBCClientTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcClient jdbcClient;

    Cliente cli1 = Cliente.builder()
            .nombre("Jose M.")
            .apellido1("Martín")
            .apellido2("Tejero")
            .ciudad("Málaga")
            .categoría(1)
            .build();

    Cliente cli2 = Cliente.builder()
            .nombre("María")
            .apellido1("Pérez")
            .apellido2("García")
            .ciudad("Granada")
            .categoría(2)
            .build();

    Cliente cli3 = Cliente.builder()
            .nombre("Javier")
            .apellido1("Gutiérrez")
            .apellido2("Martínez")
            .ciudad("Málaga")
            .categoría(3)
            .build();


    @Test
    void insertWithoutIDRecoveryTest() {

        int rowsUpdated = jdbcClient.sql(
                        """
                        INSERT INTO cliente (nombre, apellido1, apellido2, ciudad, categoría) 
                        VALUES  (     ?,         ?,         ?,       ?,         ?)
                        """)
                .param(cli1.getNombre())
                .param(cli1.getApellido1())
                .param(cli1.getApellido2())
                .param(cli1.getCiudad())
                .param(cli1.getCategoría())
                .update();

        assertEquals(rowsUpdated,1);
    }

    @Test
    void insertWithIDRecoveryTest() {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsUpdated = jdbcClient.sql(
                        """
                        INSERT INTO cliente (nombre, apellido1, apellido2, ciudad, categoría) 
                        VALUES  (     ?,         ?,         ?,       ?,         ?)
                        """)
                .param(cli1.getNombre())
                .param(cli1.getApellido1())
                .param(cli1.getApellido2())
                .param(cli1.getCiudad())
                .param(cli1.getCategoría())
                .update(keyHolder);

        cli1.setId(keyHolder.getKey().intValue());
        assertEquals(rowsUpdated,1);
        assertTrue(cli1.getId()!=null);
    }

    @Test
    void update() {

    cli1.setId(1);
        String query = """
                    UPDATE cliente 
                    SET 
                    nombre  = :nombre,
                    apellido1 = :apellido1,
                    apellido2 = :apellido2,
                    ciudad = :ciudad,
                    categoría = :categoría
                    WHERE
                    id = :id               
                """;
        int rowsUpdated = jdbcClient.sql(query)
                .paramSource(cli1)
                .update();

        assertEquals(rowsUpdated,1);
        System.out.println(rowsUpdated);
    }


    @Test
    void delete() {

        int idBorrar = 9;
        int rowsUpdated = jdbcClient.sql(
                        """
                        DELETE FROM cliente WHERE id = ?
                        """)
                .param(idBorrar)
                .update();

        assertEquals(rowsUpdated,1);

    }


    @Test
    void batch() {

    }

    @Test
    void getAll() {
        String query = """
                       SELECT * FROM cliente
                       """;

        RowMapper<Cliente> rowMapperCliente = (rs, rowNum) -> new Cliente(rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido1"),
                rs.getString("apellido2"),
                rs.getString("ciudad"),
                rs.getInt("categoría")
        );

        List<Cliente> listCli = jdbcClient.sql(query)
                .query(rowMapperCliente)
                .list();

        assertTrue(listCli.size()>0);
    }

    @Test
    void findById() {

        int id = 1;

        String query = """
                SELECT * FROM cliente WHERE ID = :id
                """;

        Optional<Cliente> optCliente = jdbcClient.sql(query)
                .param("id", id)
                .query(Cliente.class)
                .optional();

        assertTrue(optCliente.isPresent());
    }

    //A realizar por el alumno...
    @Test
    void findByNombre() {
        String nombre = "Adela";

        String query = """
                SELECT * FROM cliente WHERE nombre like :nombre
                """;
        Optional<Cliente> optionalCliente = jdbcClient.sql(query)
                .param("nombre", nombre)
                .query(Cliente.class)
                .optional();

        assertTrue(optionalCliente.isPresent());
        assertEquals(optionalCliente.get().getNombre(),nombre);
    }

    @Test
    void findByNombreButNotFound() {
        String nombre = "No existe";

        String query = """
                SELECT * FROM cliente WHERE nombre like :nombre
                """;
        Optional<Cliente> optionalCliente = jdbcClient.sql(query)
                .param("nombre", nombre)
                .query(Cliente.class)
                .optional();

        assertTrue(optionalCliente.isEmpty());
    }

    @Test
    void findClienteByCaracteristicaBetween() {
        int característicaInit = 120;
        int característicaFin = 130;

        String query = """
                SELECT * FROM cliente WHERE categoría BETWEEN ? and ?
                """;

        RowMapper<Cliente> rowMapperCliente = (rs, rowNum) -> new Cliente(rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido1"),
                rs.getString("apellido2"),
                rs.getString("ciudad"),
                rs.getInt("categoría")
        );

        List<Cliente> listCli = jdbcClient.sql(query)
                .param(característicaInit)
                .param(característicaFin)
                .query(rowMapperCliente)
                .list();

        assertEquals(listCli.size(),1);
    }

    @Test
    void findClienteByNombreContainingAndApellido1Containing() {
        String nombreContaining = "%Adela%";
        String apellido1Containing = "%Salas%";

        String query = """
                SELECT * FROM cliente WHERE nombre lIKE lower(?)
                AND apellido1 LIKE lower(?)
                """;
        Optional<Cliente> optionalCliente = jdbcClient.sql(query)
                .param(nombreContaining)
                .param(apellido1Containing)
                .query(Cliente.class)
                .optional();

        assertTrue(optionalCliente.isPresent());
    }

    @Test
    void findClienteByNombreContainingAndApellido1ContainingButNotFound() {
        String nombreContaining = "%Federico%";
        String apellido1Containing = "%Balverde%";

        String query = """
                SELECT * FROM cliente WHERE nombre lIKE lower(?)
                AND apellido1 LIKE lower(?)
                """;
        Optional<Cliente> optionalCliente = jdbcClient.sql(query)
                .param(nombreContaining)
                .param(apellido1Containing)
                .query(Cliente.class)
                .optional();

        assertTrue(optionalCliente.isEmpty());
    }

    void findPedidosWithClienteAndComercialByCliente_id() {
        int clienteId = 0;
        //TODO
    }

    void insertNewClienteAndPedido() {
        //
    }

}
