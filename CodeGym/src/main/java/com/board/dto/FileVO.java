package com.board.dto;

import lombok.*;

@Getter
@Setter
public class FileVO {
	
	private int fileseqno;
	private int seqno;
	private String userid;
	private String org_filename;
	private String stored_filename;
	private long filesize;
	private String checkfile;
	
}
