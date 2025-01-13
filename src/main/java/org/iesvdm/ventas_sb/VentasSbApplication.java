package org.iesvdm.ventas_sb;

import lombok.extern.slf4j.Slf4j;
import org.iesvdm.ventas_sb.dao.ComercialDAOJDBCClientImpl;
import org.iesvdm.ventas_sb.dao.ComercialDAOJDBCTemplateImpl;
import org.iesvdm.ventas_sb.modelo.Comercial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootApplication
public class VentasSbApplication implements CommandLineRunner {

    @Autowired
    private ComercialDAOJDBCTemplateImpl comercialDAOJDBCTemplateImpl;

    @Autowired
    private ComercialDAOJDBCClientImpl comercialDAOJDBCClientImpl;

    public static void main(String[] args) {
        SpringApplication.run(VentasSbApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //Este codigo se ejecuta despues de cargar el contexto en el transitorio de inicio de la aplicación.
        log.info("Arranca la aplicacion");
        log.info("Prueba ComercialDAO...");

        Comercial com1 = Comercial.builder()
                .nombre("Manolo")
                .apellido1("Morales")
                .apellido2("Martinez")
                .comisión(1.5f)
                .build();

        // Crear con JdbcTemplate
        comercialDAOJDBCTemplateImpl.create(com1);
        System.out.println(com1);

        // Obtener todos con JdbcTemplate
        var listaComercialesTemplate = comercialDAOJDBCTemplateImpl.getAll();
        System.out.println(listaComercialesTemplate);

        // Buscar por id en BD con JdbcTemplate
        Optional<Comercial> optCom1Template = comercialDAOJDBCTemplateImpl.find(2);
        System.out.println(optCom1Template);

        // Actualizar con JdbcTemplate
        com1.setNombre("Manoliiitoo");
        comercialDAOJDBCTemplateImpl.update(com1);
        Optional<Comercial> manolitoTemplate = comercialDAOJDBCTemplateImpl.find(com1.getId());
        System.out.println(manolitoTemplate);

        // Eliminar con JdbcTemplate
        comercialDAOJDBCTemplateImpl.delete(com1.getId());

        //-------------- Con JdbcClient-------------------------

        Comercial com2 = Comercial.builder()
                .nombre("Carlos")
                .apellido1("Torres")
                .apellido2("Lopez")
                .comisión(2.0f)
                .build();

        // Crear con JdbcClient
        comercialDAOJDBCClientImpl.create(com2);
        System.out.println(com2);

        // Obtener todos con JdbcClient
        var listaComercialesClient = comercialDAOJDBCClientImpl.getAll();
        System.out.println(listaComercialesClient);

        // Buscar por id en BD con JdbcClient
        Optional<Comercial> optCom2Client = comercialDAOJDBCClientImpl.find(2);
        System.out.println(optCom2Client);

        // Actualizar con JdbcClient
        com2.setNombre("Carlitos");
        comercialDAOJDBCClientImpl.update(com2);
        Optional<Comercial> carlitos = comercialDAOJDBCClientImpl.find(com2.getId());
        System.out.println(carlitos);

        // Eliminar con JdbcClient
        comercialDAOJDBCClientImpl.delete(com2.getId());
    }
}
