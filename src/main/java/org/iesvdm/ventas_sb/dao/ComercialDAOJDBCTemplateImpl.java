package org.iesvdm.ventas_sb.dao;

import lombok.extern.slf4j.Slf4j;
import org.iesvdm.ventas_sb.modelo.Cliente;
import org.iesvdm.ventas_sb.modelo.Comercial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ComercialDAOJDBCTemplateImpl implements ComercialDAO{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void create(Comercial comercial) {

        String sqlInsert = """
							INSERT INTO comercial (nombre, apellido1, apellido2, comisión) 
							VALUES  (?,?,?,?)
						   """;

        PreparedStatementCreator psc = connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlInsert, new String[]{"id"});
            int idx = 1;
            ps.setString(idx++, comercial.getNombre());
            ps.setString(idx++, comercial.getApellido1());
            ps.setString(idx++, comercial.getApellido2());
            ps.setFloat(idx, comercial.getComisión());
            return ps;
        };

        //Con recuperación de id generado
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsUpdated = jdbcTemplate.update(psc, keyHolder);

        comercial.setId(keyHolder.getKey().intValue());

        log.info("Insertados {} registros.", rowsUpdated);
    }

    @Override
    public List<Comercial> getAll() {

        //--------------Metodo 1 (En uso)---------------------
        RowMapper<Comercial> rowMapperComercial = (rs, rowNum) -> Comercial.builder()
                .id(rs.getInt("id"))
                .nombre(rs.getString("nombre"))
                .apellido1(rs.getString("apellido1"))
                .apellido2(rs.getString("apellido2"))
                .comisión(rs.getFloat("comisión"))
                .build();

        List<Comercial> listCom = jdbcTemplate.query(
                "SELECT * FROM comercial",
                rowMapperComercial
        );

        //-------------Metodo 2 Simplificado---------------
        List<Comercial> listCom1 = jdbcTemplate.query(
                "SELECT * FROM comercial",
                BeanPropertyRowMapper.newInstance(Comercial.class)
        );

        log.info("Devueltos {} registros.", listCom.size());

        return listCom;
    }

    @Override
    public Optional<Comercial> find(int id) {

        ResultSetExtractor<Comercial> rse = (ResultSet rs) -> {
            if (rs.next()) {
                return Comercial.builder()
                        .id(rs.getInt("id"))
                        .nombre(rs.getString("nombre"))
                        .apellido1(rs.getString("apellido1"))
                        .apellido2(rs.getString("apellido2"))
                        .comisión(rs.getFloat("comisión"))
                        .build();
            } else {
                return null;
            }
        };

        Comercial com =  jdbcTemplate
                .query("SELECT * FROM comercial WHERE id = ?",
                        rse,
                        id
                );

        return Optional.ofNullable(com);
    }

    @Override
    public void update(Comercial comercial) {

        int rows = jdbcTemplate.update("""
										UPDATE comercial SET 
														nombre = ?, 
														apellido1 = ?, 
														apellido2 = ?,
														comisión = ?  
												WHERE id = ?
										""", comercial.getNombre()
                                            , comercial.getApellido1()
                                            , comercial.getApellido2()
                                            , comercial.getComisión()
                                            , comercial.getId());

        log.info("Update de Cliente con {} registros actualizados.", rows);
    }

    @Override
    public void delete(int id) {

        int rows = jdbcTemplate.update("DELETE FROM comercial WHERE id = ?", id);

        log.info("Delete de Cliente con {} registros eliminados.", rows);
    }
}
