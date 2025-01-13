package org.iesvdm.ventas_sb;

import lombok.extern.slf4j.Slf4j;
import org.iesvdm.ventas_sb.modelo.Cliente;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JDBCTemplateTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

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


	@Order(1)
	@Test
    void insertWithoutIDRecoveryTest() {

        //Without recovery of id
		int rows = jdbcTemplate.update("""
							                  INSERT INTO cliente (nombre, apellido1, apellido2, ciudad, categoría) 
							                  VALUES  (     ?,         ?,         ?,       ?,         ?)
						                   """,
							cli1.getNombre(),
							cli1.getApellido1(),
							cli1.getApellido2(),
							cli1.getCiudad(),
							cli1.getCategoría()
					);


        log.info("{} inserted records.", rows);

		assertEquals(1, rows);
		assertTrue(cli2.getId() == null);

    }

	@Order(2)
	@Test
	void insertWithIDRecoveryTest() {

		//With recovery of id
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int rows = jdbcTemplate.update(
				connection -> {

					PreparedStatement ps = connection.prepareStatement(
							"""
							INSERT INTO cliente (nombre, apellido1, apellido2, ciudad, categoría) 
							VALUES  (     ?,         ?,         ?,       ?,         ?)
						    """
							, new String[]{"id"});

					int idx = 1;
					ps.setString(idx++, cli2.getNombre());
					ps.setString(idx++, cli2.getApellido1());
					ps.setString(idx++, cli2.getApellido2());
					ps.setString(idx++, cli2.getCiudad());
					ps.setInt(idx, cli2.getCategoría());

					return ps;

				}, keyHolder);

		cli2.setId(keyHolder.getKey().intValue());

		log.info("{} inserted records.", rows);

		assertEquals(1, rows);
		assertTrue(cli2.getId() > 0);
	}

	@Test
	void insertWithSimpleInsertJbdc() {

		SimpleJdbcInsert simpleJdbcInsert =
				new SimpleJdbcInsert(jdbcTemplate.getDataSource())
						.withTableName("cliente")
						.usingGeneratedKeyColumns("id");

		Map<String, Object> params = new HashMap<>();
		params.put("nombre", cli3.getNombre());
		params.put("apellido1", cli3.getApellido1());
		params.put("apellido2", cli3.getApellido2());
		params.put("ciudad", cli3.getCiudad());
		params.put("categoría", cli3.getCategoría());

		int id = simpleJdbcInsert.executeAndReturnKey(params).intValue();

		assertTrue(id > 0);
	}

	@Test
	void insertWithSimpleInsertJbdcBeanProperty() {

		SimpleJdbcInsert simpleJdbcInsert =
				new SimpleJdbcInsert(jdbcTemplate.getDataSource())
						.withTableName("cliente")
						.usingGeneratedKeyColumns("id");

		BeanPropertySqlParameterSource beanProps= new BeanPropertySqlParameterSource(cli3);

		int id = simpleJdbcInsert.executeAndReturnKey(beanProps).intValue();

		assertTrue(id > 0);

	}

	/*
CREATE PROCEDURE read_cliente (
	IN in_id INTEGER,
	OUT out_nombre VARCHAR(100),
	OUT out_apellidos VARCHAR(100),
	OUT out_categoria INTEGER)
BEGIN
	SELECT nombre, concat(apellido1, apellido2), categoría
	INTO out_nombre, out_apellidos, out_categoria
	FROM cliente where id = in_id;
END;
	 */
	@Test
	void callProcedureSimpleJdbcCall() {

		SimpleJdbcCall sJdbcCall = new SimpleJdbcCall(jdbcTemplate)
				.withProcedureName("read_cliente");

		int id = 1;
		SqlParameterSource in = new MapSqlParameterSource()
				.addValue("in_id", id);

		Map<String, Object> out = sJdbcCall.execute(in);
		System.out.println(out);
	}

	@Test
	void update() {

		cli1.setCategoría(7);
		int rows = jdbcTemplate.update("""
										UPDATE cliente SET 							
														categoría = ?  
												WHERE id = ?
										""",
				cli1.getCategoría(),
				cli1.getId());

		log.info("Update de Cliente con {} registros actualizados.", rows);

		assertEquals(1, rows);
	}

	@Test
	void delete() {

		int rows = jdbcTemplate.update("""
										DELETE FROM cliente  
										WHERE id = ?
										""",
				cli1.getId());

		log.info("Delete de Cliente con {} registros actualizados.", rows);

		assertEquals(1, rows);

	}

	@Test
	void batch() {

		int countInit = jdbcTemplate.queryForObject("""
				SELECT COUNT(*) FROM cliente
				""", Integer.class);

		byte[] array = new byte[7];
		Random random = new Random();

		List<Cliente> listCliRandom = IntStream.rangeClosed(1, 500).boxed().map(integer -> {

			String[] generatedStringArr = new String[4];
			IntStream.range(0,generatedStringArr.length).forEach( i -> {
					random.nextBytes(array);
					String generatedString = new String(array, Charset.forName("UTF-8"));
					generatedStringArr[i] = generatedString;
				}
			);

			int idx = 0;
			return Cliente.builder()
					.nombre(generatedStringArr[idx++])
					.apellido1(generatedStringArr[idx++])
					.apellido2(generatedStringArr[idx++])
					.ciudad(generatedStringArr[idx++])
					.categoría(random.nextInt(1,11))
					.build();

				}).toList();

		int batchSize = 100;
		jdbcTemplate.batchUpdate("""
								INSERT INTO cliente (nombre, apellido1, apellido2, ciudad, categoría)
								VALUES  (     ?,         ?,         ?,       ?,         ?)
								""", listCliRandom, batchSize,
				(PreparedStatement ps, Cliente cliente) -> {
					int idx = 1;
					ps.setString(idx++, cliente.getNombre());
					ps.setString(idx++, cliente.getApellido1());
					ps.setString(idx++, cliente.getApellido2());
					ps.setString(idx++, cliente.getCiudad());
					ps.setInt(idx++, cliente.getCategoría());
				});

		int countEnd = jdbcTemplate.queryForObject("""
				SELECT COUNT(*) FROM cliente
				""", Integer.class);

		assertEquals(500, countEnd - countInit);
	}

	@Test
	void getAll() {

		List<Cliente> listCli = jdbcTemplate.query("""
				SELECT * FROM cliente
				""", BeanPropertyRowMapper.newInstance(Cliente.class)
					);

		listCli.forEach(System.out::println);

	}

	@Test
	void findById() {
		int idToFind = 1;
		Optional<Cliente> optCli = jdbcTemplate.query("""
				SELECT * FROM cliente WHERE id = ?
				""", rs -> {

			if (rs.next()) {
				return Optional.of(UtilDAO.buildCliente(rs));
			} else {
				return Optional.empty();
			}

		}, idToFind);

		assertTrue(optCli.isPresent());
		assertEquals(idToFind, optCli.get().getId());

	}

	//A realizar por el alumno...
	@Test
	void findByNombre() {

		String nombre = "Adela";

		Optional<Cliente> optCli = jdbcTemplate.query("""
				SELECT * FROM cliente WHERE nombre = ?
				""", rs -> {

			if (rs.next()) {
				return Optional.of(UtilDAO.buildCliente(rs));
			} else {
				return Optional.empty();
			}

		}, nombre);

		assertTrue(optCli.isPresent());
		assertEquals(nombre, optCli.get().getNombre());
	}

	@Test
	void findByNombreButNotFound() {
		String nombre = "Gregorio";
		Optional<Cliente> optCli = jdbcTemplate.query("""
				SELECT * FROM cliente WHERE nombre = ?
				""", rs -> {

			if (rs.next()) {
				return Optional.of(UtilDAO.buildCliente(rs));
			} else {
				return Optional.empty();
			}

		}, nombre);

		assertTrue(optCli.isEmpty());
	}

	@Test
	void findClienteByCategoriaBetween() {
		int categoriaInit = 100;
		int categoriaFin = 200;

		List<Cliente> listCli = jdbcTemplate.query("""
				SELECT * FROM cliente WHERE categoría BETWEEN ? AND ?
				""",
				BeanPropertyRowMapper.newInstance(Cliente.class),
				categoriaInit,
				categoriaFin
		);

		listCli.forEach(System.out::println);

		assertEquals(6,listCli.size());
	}

	@Test
	void findClienteByNombreContainingAndApellido1Containing() {
		String nombreContaining = "Adela";
		String apellido1Containing = "%Salas%";

		List<Cliente> listCli = jdbcTemplate.query("""
				SELECT * FROM cliente c 
         		WHERE lower(c.nombre) LIKE lower(?)
				AND lower(c.apellido1) like lower(?)
				""",
				BeanPropertyRowMapper.newInstance(Cliente.class),
				nombreContaining,
				apellido1Containing
		);

		assertEquals(listCli.size(),1);
	}

	@Test
	void findClienteByNombreContainingAndApellido1ContainingButNotFound() {
		String nombreContaining = "No existe";
		String apellido1Containing = "No existe";

		List<Cliente> listCli = jdbcTemplate.query("""
				SELECT * FROM cliente c 
         		WHERE lower(c.nombre) LIKE lower(?)
				AND lower(c.apellido1) like lower(?)
				""",
				BeanPropertyRowMapper.newInstance(Cliente.class),
				nombreContaining,
				apellido1Containing
		);

		assertEquals(listCli.size(),0);
	}

	@Test
	void findPedidosWithClienteAndComercialByCliente_id() {
		int clienteId = 0;

		List<Cliente> listCli = jdbcTemplate.query("""
				SELECT * FROM cliente c JOIN pedido p ON p.id_cliente = c.id
				""",
				BeanPropertyRowMapper.newInstance(Cliente.class),
				clienteId
		);
		System.out.println(listCli);
	}

	void insertNewClienteAndPedido() {
		//
	}

}
