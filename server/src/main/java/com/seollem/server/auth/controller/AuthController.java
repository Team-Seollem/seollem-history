package com.seollem.server.auth.controller;

import com.seollem.server.dto.SingleResponseDto;
import com.seollem.server.member.dto.MemberDto;
import com.seollem.server.member.entity.Member;
import com.seollem.server.member.mapper.MemberMapper;
import com.seollem.server.member.repository.MemberRepository;
import com.seollem.server.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Validated
public class AuthController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    public AuthController(MemberService memberService, MemberMapper memberMapper) {
        this.memberService = memberService;
        this.memberMapper = memberMapper;
    }

/*
        로그인 요청의 경우 UsernamePasswordAuthenticationFilter 에서 PathMatcher 로 /login 과 POST 를 지정하고 있다.
        따라서 "/login" 으로 요청할 경우 자동으로 인터셉트 되어 로그인이 진행되므로 따로 컨트롤러는 작성하지 않는다.
     */


    @PostMapping("/join")
    public ResponseEntity join(@Valid @RequestBody MemberDto.Post requestBody) {
        Member member = memberMapper.memberPostToMember(requestBody);

        memberService.creatMember(member);
        return new ResponseEntity<>("회원 가입 성공", HttpStatus.CREATED);
    }
}
