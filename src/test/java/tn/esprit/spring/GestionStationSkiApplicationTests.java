package tn.esprit.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // ADD THIS ANNOTATION
public class GestionStationSkiApplicationTests {

	@Autowired // ADD THESE 3 LINES
	private MockMvc mockMvc;

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() { // KEEP THIS EXISTING TEST
	}

	// ADD THESE NEW TESTS:
	@Test
	void healthCheck_ShouldReturn200() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk()); // Verify health endpoint
	}

	@Test
	void databaseConnection_ShouldWork() throws Exception {
		try (Connection conn = dataSource.getConnection()) {
			assertTrue(conn.isValid(1)); // Verify DB connection
		}
	}

	@Test
	void publicApi_ShouldBeAccessible() throws Exception {
		mockMvc.perform(get("/piste/all"))
				.andExpect(status().isOk()); // Verify API endpoint
	}
}