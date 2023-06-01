package com.board.dto;

import lombok.*;

@Getter
@Setter
public class BoardVO {
	
    private int seqno;
    private int seq;
    private String userid;
    private String writer;
    private String title;
    private String regdate;
    private String content;
    private int hitno;
    private int likecnt;
	private int dislikecnt;
	
}
