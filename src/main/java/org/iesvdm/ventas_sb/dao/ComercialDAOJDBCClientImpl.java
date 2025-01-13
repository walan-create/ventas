package org.iesvdm.ventas_sb.dao;

import lombok.extern.slf4j.Slf4j;
import org.iesvdm.ventas_sb.modelo.Comercial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ComercialDAOJDBCClientImpl implements ComercialDAO {

    @Autowired
    private JdbcClient jdbcClient;

    @Override
    public void create(Comercial comercial) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsUpdated = jdbcClient.sql(""" 
                        INSERT INTO comercial (nombre, apellido1, apellido2, comisión) 
                        VALUES (?, ?, ?, ?) 
                        """)
                .param(comercial.getNombre())
                .param(comercial.getApellido1())
                .param(comercial.getApellido2())
                .param(comercial.getComisión())
                .update(keyHolder);

        comercial.setId(keyHolder.getKey().intValue());

        log.info("Insertados {} registros", rowsUpdated);
    }

    @Override
    public List<Comercial> getAll() {

        String query = """
                       SELECT * FROM comercial
                       """;

        RowMapper<Comercial> rowMapperComercial = (rs, rowNum) -> new Comercial(rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido1"),
                rs.getString("apellido2"),
                rs.getFloat("comisión")
        );

        List<Comercial> listComercial = jdbcClient.sql(query)
                .query(rowMapperComercial)
                .list();

        return listComercial;
    }

    @Override
    public Optional<Comercial> find(int id) {

        String query = """
                SELECT * FROM comercial WHERE id = :id
                """;

        Optional<Comercial> optComercial = jdbcClient.sql(query)
                .param("id", id)
                .query(Comercial.class)
                .optional();

        return optComercial;
    }

    @Override
    public void update(Comercial comercial) {

        String query = """
                    UPDATE comercial 
                    SET 
                    nombre  = :nombre,
                    apellido1 = :apellido1,
                    apellido2 = :apellido2,
                    comisión = :comisión
                    WHERE
                    id = :id               
                """;
        int rowsUpdated = jdbcClient.sql(query)
                .paramSource(comercial)
                .update();

        log.info("Actualizados {} registros", rowsUpdated);

    }

    @Override
    public void delete(int id) {

        int rowsUpdated = jdbcClient.sql("DELETE FROM comercial WHERE id = ?")
                .param(id)
                .update();

        log.info("Borrados {} registros", rowsUpdated);
    }
}
