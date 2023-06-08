package com.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExerciseController {
	
	@GetMapping("/exercise/freeWeight/index")
	public void getIndex() {
		
	}

	@GetMapping("/exercise/freeWeight/Chest3")
	public void getChest() {

	}

	@GetMapping("/exercise/abdominals")
	public void getAbs() {

	}

}
