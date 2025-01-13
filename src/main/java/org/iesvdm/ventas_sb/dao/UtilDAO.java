package org.iesvdm.ventas_sb.dao;

import org.iesvdm.ventas_sb.modelo.Cliente;
import org.iesvdm.ventas_sb.modelo.Comercial;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilDAO {

    public static Cliente buildCliente(ResultSet rs) throws SQLException {
        return Cliente.builder()
                .id(rs.getInt("id"))
                .nombre(rs.getString("nombre"))
                .apellido1(rs.getString("apellido1"))
                .apellido2(rs.getString("apellido2"))
                .ciudad(rs.getString("ciudad"))
                .categoría(rs.getInt("categoría"))
                .build();
    }

    public static Comercial buildComercial(ResultSet rs) throws SQLException {
        return Comercial.builder()
                .id(rs.getInt("id"))
                .nombre(rs.getString("nombre"))
                .apellido1(rs.getString("apellido1"))
                .apellido2(rs.getString("apellido2"))
                .comisión(rs.getInt("comisión"))
                .build();
    }

}
