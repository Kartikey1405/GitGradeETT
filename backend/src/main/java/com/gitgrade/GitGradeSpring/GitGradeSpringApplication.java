//package com.gitgrade.GitGradeSpring;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class GitGradeSpringApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(GitGradeSpringApplication.class, args);
//	}
//
//}

package com.gitgrade.GitGradeSpring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GitGradeSpringApplication {

	public static void main(String[] args) {
		// --- 1. Load the .env file ---
		// This makes sure Java can see GITHUB_TOKEN, GEMINI_API_KEY, etc.
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		// --- 2. Inject them into System Properties ---
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		// --- 3. Start the App ---
		SpringApplication.run(GitGradeSpringApplication.class, args);
	}

}
