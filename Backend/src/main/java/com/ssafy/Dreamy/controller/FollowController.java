package com.ssafy.Dreamy.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.Dreamy.model.MemberDto;
import com.ssafy.Dreamy.model.service.FollowService;

@CrossOrigin(origins = { "http://localhost:3000" })
@RestController
@RequestMapping("/account")
public class FollowController {

	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	@Autowired
	FollowService followservice;

	////////// 팔로우 요청 ///////////
	@PostMapping("/follow")
	public ResponseEntity<Map<String, Object>> follow(HttpServletRequest request, Model model) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;

		String clickUser = request.getParameter("login_id");
		String followedUser = request.getParameter("target_id");

		int user_id = Integer.parseInt(clickUser);
		int target_id = Integer.parseInt(followedUser);

		try {
			System.out.println("--팔로우 요청");
			followservice.followService(user_id, target_id);
			resultMap.put("message", SUCCESS);
			status = HttpStatus.ACCEPTED;
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("--팔로우 실패"); //
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);

	}

	////////// 팔로우 취소 ///////////
	@DeleteMapping
	public ResponseEntity<Map<String, Object>> unfollow(HttpServletRequest request, Model model) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;

		String clickUser = request.getParameter("login_id");
		String followedUser = request.getParameter("target_id");

		int user_id = Integer.parseInt(clickUser);
		int target_id = Integer.parseInt(followedUser);

		try {
			System.out.println("--팔로우 취소요청");
			followservice.unfollowService(user_id, target_id);
			resultMap.put("message", SUCCESS);
			status = HttpStatus.ACCEPTED;
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("--팔로우 취소실패"); //
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	////////// 팔로우 관계 검증 ///////////
	@GetMapping
	public ResponseEntity<Map<String, Object>> relationcheck(HttpServletRequest request, Model model) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;

		String clickUser = request.getParameter("login_id");
		String followedUser = request.getParameter("target_id");

		int user_id = Integer.parseInt(clickUser);
		int target_id = Integer.parseInt(followedUser);

		if (followservice.followcheck(user_id, target_id)) {
			System.out.println("--친구관계성립");
			resultMap.put("message", SUCCESS);
			status = HttpStatus.ACCEPTED;
		} else {
			System.out.println("--친구관계아니다");
			resultMap.put("mwssage", FAIL);
			status = HttpStatus.UNAUTHORIZED;

		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
}
