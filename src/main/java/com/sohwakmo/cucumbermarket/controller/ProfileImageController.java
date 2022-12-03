package com.sohwakmo.cucumbermarket.controller;

import com.sohwakmo.cucumbermarket.dto.ProfileImageReadDto;
import com.sohwakmo.cucumbermarket.service.MypageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/profileImage")
public class ProfileImageController {

    private final MypageService mypageService;

    @GetMapping("/{memberNo}")
    @ResponseBody
    public ResponseEntity<ProfileImageReadDto> changeImage(@PathVariable Integer memberNo){
        log.info("changeImage(memberNo={})", memberNo);

        ProfileImageReadDto dto = mypageService.readProfileImage(memberNo);
        return  ResponseEntity.ok(dto);
    }

    @PostMapping("/{modalMemberNo}")
    public ResponseEntity<Integer> updateImage(@PathVariable Integer modalMemberNo, @RequestBody ProfileImageReadDto dto) {
        log.info("updateImage(memberNo={}, dto={})", modalMemberNo, dto);

        dto.setMemberNo(modalMemberNo);

        Integer result = mypageService.updateImage(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload")
    public ResponseEntity<Integer> uploadImage(@RequestBody MultipartFile userProfileImage) throws IOException {
        log.info("uploadImage(file={})", userProfileImage);

        log.info("getFilename={}", userProfileImage.getOriginalFilename());
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/images/mypage";
        log.info("image upload={}", projectPath);
        File saveFile = new File(projectPath, userProfileImage.getOriginalFilename());
        userProfileImage.transferTo(saveFile);
        return null;
    }


    @GetMapping("/userImage/{memberNo}")
    public ResponseEntity<ProfileImageReadDto> readUserImage(@PathVariable Integer memberNo){
        log.info("readUserImage(memberNo={})", memberNo);

        ProfileImageReadDto dto = mypageService.readProfileImage(memberNo);

        return ResponseEntity.ok(dto);
    }
}
