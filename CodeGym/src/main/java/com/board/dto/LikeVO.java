package com.board.dto;

import lombok.*;

@Getter
@Setter
public class LikeVO {

	private int seqno;
	private String userid;
	private String mylikecheck;
	private String mydislikecheck;
	private String likedate;
	private String dislikedate;
	
}
