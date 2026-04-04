package com.digiwork.taskhive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.digiwork.taskhive.integration.BaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class TaskhiveBackendApplicationTests extends BaseIntegrationTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
	}

}
