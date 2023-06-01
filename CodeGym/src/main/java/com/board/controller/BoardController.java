package com.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

	@GetMapping("/exercise/freeWeight")
	public void getExercise() {
	}
	
	@GetMapping("/exercise/chest")
	public void getChest() {
		
	}
	@GetMapping("/exercise/abdominals")
	public void getAbs() {
		
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
