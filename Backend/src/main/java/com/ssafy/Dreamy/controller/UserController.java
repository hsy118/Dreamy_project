package com.ssafy.Dreamy.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.Dreamy.model.UserDto;
import com.ssafy.Dreamy.model.service.JwtServiceImpl;
import com.ssafy.Dreamy.model.service.UserService;

//@CrossOrigin(origins = { "*" }, maxAge = 6000)
@CrossOrigin(origins = { "http://localhost:3000" })
@RestController
@RequestMapping("/account")
public class UserController {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	@Autowired
	private JwtServiceImpl jwtService;

	@Autowired
	private UserService userService;

	// 로그인
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody UserDto memberDto) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		String email = memberDto.getEmail();
		String password = memberDto.getPassword();
		try {
			boolean isUser = userService.login(email, password);
			System.out.println("1.로그인 시도"); //
			if (isUser) {
				String token = jwtService.create("userEmail", email, "access-token");	// key, data, subject
				logger.debug("로그인 토큰정보 : {}", token);
				resultMap.put("access-token", token);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
				System.out.println("2-1토큰 생성");
			} else {
				resultMap.put("message", FAIL);
				status = HttpStatus.NOT_FOUND;
				System.out.println("2-2로그인 실패");
			}
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("2-3 서버 오류 로그인 실패"); //
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@PostMapping("/checkJwt")
	public ResponseEntity<Map<String, Object>> jwtOauth(@RequestBody String token) throws IOException{
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		System.out.println("3-1jwt 함수 진입"); 
		try {
			status = HttpStatus.ACCEPTED;
			String email=jwtService.get(token);
			UserDto loginUser=userService.setUser(email);
			System.out.println("로그인 완료");
			
			System.out.println(loginUser.getLoginType());
			resultMap.put("user", loginUser);
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("3-2 jwt 인증 실패"); //
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	@PostMapping("/checkUser")
		public ResponseEntity<Map<String, Object>> checkUser(@RequestBody UserDto memberDto) throws IOException{
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		String email=memberDto.getEmail();
		String type=memberDto.getLoginType();
		System.out.println("1. socail 로그인 db 유저 정보 확인"); 

		try {
			//이메일 중복 검사
			int user=userService.getEmail(email);
			if(user==0) {	// db에 유저 정보가 없음 => 자동가입 시키기
				resultMap.put("message", "needSignup");
				status = HttpStatus.ACCEPTED;
				System.out.println("2-1 소셜 계정 자동 가입"); 
			}
			else if(user==1){//db에 유저정보가 있음 => 로그인
				if(!(type.equals(userService.getLoginType(email)))) {	//db에 존재하는 이메일이 현재 로그인하는 소셜타입과 맞지 않으면 거부
					resultMap.put("message", "otherSocialLogin");					
					status = HttpStatus.ACCEPTED;
					System.out.println("2-2 소셜 계정존재시 거부");
				}else{// 맞으면 자동 로그인
					String token = jwtService.create("userEmail", email, "access-token");
					resultMap.put("access-token", token);
					resultMap.put("message", "success");
					status = HttpStatus.ACCEPTED;
					System.out.println("2-3 소셜 계정 자동 로그인");
				}
			}
		}catch(Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
			return new ResponseEntity<Map<String, Object>>(resultMap, status);
			
	}
	// 회원가입
	@PostMapping("/signup")
	public ResponseEntity<Map<String, Object>> signup(@RequestBody UserDto userDto) {
		Map<String, Object> resultMap = new HashMap<>();
		if(userDto.getPassword()==null) {
			// 초기비밀번호 설정
			// 난수생성 함수 추가
			userDto.setPassword("1q2w3e4r");
		}
		HttpStatus status = null;
		String type=userDto.getLoginType();
		System.out.println(type+"type: signup");
		try {
			System.out.println("1.회원가입 시도");
			int emailNum = userService.getEmail(userDto.getEmail());
			int nameNum = userService.getName(userDto.getName());
			if (emailNum != 0) {
				resultMap.put("message", "동일한 이메일이 사용중입니다.");
				status = HttpStatus.CONFLICT;
				System.out.println("2-1이메일 중복");
			} else if (nameNum != 0) {
				resultMap.put("message", "동일한 닉네임이 사용중입니다.");
				status = HttpStatus.CONFLICT;
				System.out.println("2-2닉네임 중복");
			} else {
				userService.signup(userDto);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
				System.out.println("3-1 회원가입 성공");
			}
		} catch (Exception e) {
			logger.error("회원가입 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("4-1 회원가입 실패");
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	/*
	// 회원탈퇴 or 정보수정 시 비밀번호 검증, 미완성
	@PostMapping("/confirm/{uid}")
	public ResponseEntity<Map<String, Object>> login(@PathVariable("uid") int uid, @RequestBody UserDto memberDto) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		try {
			UserDto loginUser = userService.login(email, password);
			if (loginUser != null) {
				String token = jwtService.create("userid", loginUser.getEmail(), "access-token");// key, data, subject
				logger.debug("로그인 토큰정보 : {}", token);
				resultMap.put("access-token", token);
				resultMap.put("user", loginUser);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
				System.out.println("--토큰 생성");
			} else {
				resultMap.put("message", FAIL);
				status = HttpStatus.NOT_FOUND;
				System.out.println("--로그인 실패");
			}
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("--로그인 실패"); //
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	*/
	
	// 회원탈퇴
	@DeleteMapping("/delete/{uid}")
	public ResponseEntity<Map<String, Object>> userDelete(@PathVariable("uid") int uid, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
		
		if (jwtService.isUsable(request.getHeader("access-token"))) {
			logger.info("사용 가능한 토큰!!!");
			try {
				userService.delete(uid);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} catch (Exception e) {
				logger.error("회원탈퇴 실패 : {}", e);
				resultMap.put("message", e.getMessage());
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			logger.error("사용 불가능 토큰!!!");
			resultMap.put("message", FAIL);
			status = HttpStatus.UNAUTHORIZED;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	// 회원정보수정
	@PutMapping("/update/{uid}")
	public ResponseEntity<Map<String, Object>> userUpdate(@PathVariable("uid") int uid, @RequestBody UserDto memberDto, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
//		if (jwtService.isUsable(request.getHeader("access-token"))) {
			logger.info("사용 가능한 토큰!!!");
			try {
				System.out.println("--회원정보 수정 시도");
				userService.update(memberDto);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
				System.out.println("--회원정보 수정 성공");
			} catch (Exception e) {
				logger.error("회원정보 수정 실패 : {}", e);
				resultMap.put("message", e.getMessage());
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				System.out.println("--회원정보 수정 실패");
			}
//		} else {
//			logger.error("사용 불가능 토큰!!!");
//			resultMap.put("message", FAIL);
//			status = HttpStatus.UNAUTHORIZED;
//		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	// 회원정보 인증, frontend와 협의
	/*@PostMapping("/userCert") // 파라미터 확인
	public ResponseEntity<Map<String, Object>> userCert(@PathVariable("email") String email, @PathVariable("phone") String phone,
			HttpServletRequest request) {*/
	@GetMapping("/userCert")
	public ResponseEntity<Map<String, Object>> userCert(@RequestParam("email") String email, @RequestParam("phone") String phone,
			HttpServletRequest request){
		
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
			
		try {
			System.out.println("--회원정보 인증 시도");
	
			if(userService.certification(email, phone) != 0) {
				resultMap.put("message", SUCCESS);		
				status = HttpStatus.ACCEPTED;
				System.out.println("--회원정보 인증 성공 ");
			}
			else {
				resultMap.put("message", "회원정보 인증 실패");
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				System.out.println("--회원정보 인증 실패"); //
			}		
		} catch (Exception e) {
			logger.error("회원정보 인증 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("--회원정보 인증 실패"); //
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	// 비밀번호 변경, frontend와 협의
	@PutMapping("/changePassword") // 파라미터 확인
	public ResponseEntity<Map<String, Object>> changePassword(@PathVariable("email") String email, @PathVariable("password") String password,
			HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
		
		try {
			System.out.println("--비밀번호 변경 시도");
			
			userService.updatePassword(email, password);
				
			resultMap.put("message", SUCCESS);				
			status = HttpStatus.ACCEPTED;			
			System.out.println("--비밀번호 변경 성공 ");
		} catch (Exception e) {
			logger.error("비밀번호 변경 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			System.out.println("--비밀번호 변경 실패"); //
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	
//	@GetMapping("/info/{userid}")
//	public ResponseEntity<Map<String, Object>> getInfo(@PathVariable("userid") String userid,
//			HttpServletRequest request) {
////		logger.debug("userid : {} ", userid);
//		Map<String, Object> resultMap = new HashMap<>();
//		HttpStatus status = HttpStatus.ACCEPTED;
//		if (jwtService.isUsable(request.getHeader("access-token"))) {
//			logger.info("사용 가능한 토큰!!!");
//			try {
////				로그인 사용자 정보.
//				UserDto memberDto = userService.userInfo(userid);
//				resultMap.put("userInfo", memberDto);
//				resultMap.put("message", SUCCESS);
//				status = HttpStatus.ACCEPTED;
//			} catch (Exception e) {
//				logger.error("정보조회 실패 : {}", e);
//				resultMap.put("message", e.getMessage());
//				status = HttpStatus.INTERNAL_SERVER_ERROR;
//			}
//		} else {
//			logger.error("사용 불가능 토큰!!!");
//			resultMap.put("message", FAIL);
//			status = HttpStatus.ACCEPTED;
//		}
//		return new ResponseEntity<Map<String, Object>>(resultMap, status);
//	}

}