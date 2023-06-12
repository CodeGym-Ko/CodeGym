package com.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class UserVO {
	
	private String userid;
	private String username;
	private String password;
	private String telno;
	private String email;
	private String authkey;
	private String role;
	private String org_filename;
	private String stored_filename;
	private long filesize;
	
	@Override
	public String toString() {
		return "UserVO [userid=" + userid + ", username=" + username + ", password=" + password 
				+", telno=" + telno + ", email=" + email + ", authkey=" + authkey 
				+ ", role=" + role + ", org_filename=" + org_filename + ", stored_filename="
				+ stored_filename + ", filesize=" + filesize + "]";
	}
}
