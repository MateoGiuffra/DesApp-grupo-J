package com.desapp.football_api;

import com.desapp.football_api.security.KeyGenerator;
import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class FootballApiApplication {

	public static void main(String[] args) {
		try {
			Server tcpServer = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-ifNotExists").start();
			System.out.println("H2 TCP server started at port: " + tcpServer.getPort());
			KeyGenerator.main(args);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		SpringApplication.run(FootballApiApplication.class, args);
	}


}
