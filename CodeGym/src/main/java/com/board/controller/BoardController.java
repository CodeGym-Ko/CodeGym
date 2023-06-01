package com.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

	@GetMapping("/user/signup")
	public void getSignup() {
	}
	
	@GetMapping("/exercise/freeWeight")
	public void getExercise() {
		
	}
	@GetMapping("/exercise/chest")
	public void getChest() {
		
	}
	
	@GetMapping("/nutritional/bulkup")
    public void getBulkup() {
    }

	@GetMapping("/nutritional/diet")
    public void getDiet() {
    }

	@GetMapping("/nutritional/bmi")
    public void getBmi() {
    }

}
