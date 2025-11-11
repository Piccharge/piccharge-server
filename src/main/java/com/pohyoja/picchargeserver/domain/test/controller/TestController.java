package com.pohyoja.picchargeserver.domain.test.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import com.pohyoja.picchargeserver.domain.family.dto.response.FamilyResponse;
import com.pohyoja.picchargeserver.domain.family.entity.Family;
import com.pohyoja.picchargeserver.domain.family.repository.FamilyRepository;
import com.pohyoja.picchargeserver.domain.family.service.FamilyService;
import com.pohyoja.picchargeserver.domain.member.dto.MemberDTO;
import com.pohyoja.picchargeserver.domain.member.entity.Member;
import com.pohyoja.picchargeserver.domain.member.repository.MemberRepository;
import com.pohyoja.picchargeserver.domain.member.service.MemberService;
import com.pohyoja.picchargeserver.domain.photo.dto.request.PhotoAddRequest;
import com.pohyoja.picchargeserver.domain.photo.dto.request.ReactionAddRequest;
import com.pohyoja.picchargeserver.domain.photo.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
//@Tag(name = "테스트 API", description = "테스트 데이터 생성을 위한 API")
public class TestController {

    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final MemberService memberService;
    private final FamilyService familyService;

    private int userCounter = 1; // 유저 번호를 관리하는 카운터
    private Random random = new Random();
    private final PhotoService photoService;

    @Operation(summary = "authorizations 테스트용 firebase ID Token 로그인 정보 가져오기", deprecated = true)
    @GetMapping("/test")
    public BaseResponse<JwtUserDetails> test(@AuthenticationPrincipal JwtUserDetails userDetails) {
        return BaseResponse.onSuccess(userDetails);
    }

    /**
     * 테스트 데이터를 생성합니다.
     *
     * @param familyCount      생성할 가족 수 (기본값: 2)
     * @param membersPerFamily 각 가족별 멤버 수 (콤마로 구분, 기본값: "3,6")
     * @return 생성된 테스트 데이터
     */
    @Operation(summary = "테스트용 임시 데이터 생성하기", description = "가족 2개, 유저 9명, 유저랑 사진과 반응 랜덤 생성", deprecated = true)
    @PostMapping("/test/create-test-data")
    public BaseResponse<Map<String, Object>> createTestData(
            @RequestParam(defaultValue = "2") int familyCount,
            @RequestParam(defaultValue = "3,6") String membersPerFamily) {

        log.info("테스트 데이터 생성 시작: 가족 {}개", familyCount);
        Map<String, Object> result = new HashMap<>();

        // membersPerFamily 문자열을 정수 배열로 변환
        String[] memberCountsStr = membersPerFamily.split(",");
        int[] memberCounts = new int[familyCount];

        // 입력된 멤버 수를 배열에 저장
        for (int i = 0; i < familyCount; i++) {
            if (i < memberCountsStr.length) {
                try {
                    memberCounts[i] = Integer.parseInt(memberCountsStr[i].trim());
                } catch (NumberFormatException e) {
                    memberCounts[i] = 3; // 기본값 3
                }
            } else {
                memberCounts[i] = 3; // 기본값 3
            }
        }

        // 가족별 데이터 생성
        for (int i = 0; i < familyCount; i++) {
            Map<String, Object> familyResult = createTestFamily(memberCounts[i]);
            result.putAll(familyResult);
        }

        log.info("테스트 데이터 생성 완료, 총 생성된 유저 수: {}", userCounter - 1);
        return BaseResponse.onSuccess(result);
    }

    /**
     * 테스트용 가족 데이터를 생성합니다.
     *
     * @param memberCount 생성할 가족 구성원 수
     * @return 생성된 가족 정보가 담긴 Map
     */
    private Map<String, Object> createTestFamily(int memberCount) {
        Map<String, Object> result = new HashMap<>();

        if (memberCount <= 0) {
            log.warn("가족 구성원 수는 1명 이상이어야 합니다.");
            return result;
        }

        List<Member> familyMembers = new ArrayList<>();

        // 모든 멤버 생성 (숫자로 된 이름 부여)
        for (int i = 0; i < memberCount; i++) {
            String userName = "유저" + userCounter++;
            String userUid = generateRandomUid(20);
            String userEmail = "user" + (userCounter - 1) + "@test.com";

            MemberDTO memberDTO = memberService.saveMember(userName, userUid, userEmail);
            Member member = memberRepository.findById(memberDTO.id()).orElseThrow();
            familyMembers.add(member);
        }

        // 첫 번째 멤버를 가족의 대표로 지정하여 가족 생성
        if (!familyMembers.isEmpty()) {
            Member familyRepresentative = familyMembers.get(0);

            FamilyResponse familyResponse = familyService.createFamily(familyRepresentative.getUid());
            Family family = familyRepository.findById(familyResponse.id()).orElseThrow();
            String inviteCode = familyService.createInviteCode(family.getId(), familyRepresentative.getUid()).code();

            // 첫 번째 멤버(가족 대표)를 제외한 나머지 멤버들을 가족에 초대
            for (int i = 1; i < familyMembers.size(); i++) {
                familyService.joinFamilyByInviteCode(inviteCode, familyMembers.get(i).getUid());
            }

            // 모든 멤버들이 사진 랜덤 개수 업로드, 모두 랜덤 리액션 추가
            int photoCount = 0;
            for (int i = 0; i < familyMembers.size(); i++) {
                int randomPhotoCount = random.nextInt(5) + 1;
                for (int j = 0; j < randomPhotoCount; j++) {
                    photoCount++;
                    PhotoAddRequest photoAddRequest = new PhotoAddRequest(
                            UUID.fromString(UUID.randomUUID().toString().toUpperCase()),
                            "https://" + generateRandomUid(5) + ".com/" + familyResponse.id() * 10 + i);
                    UUID newPhotoID = photoService.addPhoto(family.getId(), familyMembers.get(i).getUid(),
                                    photoAddRequest)
                            .id();
                    ReactionAddRequest reactionAddRequest = generateRandomReaction();
                    photoService.addReaction(family.getId(), newPhotoID, familyMembers.get(i).getUid(),
                            reactionAddRequest);
                }
            }

            log.info("{}번 가족 생성 완료, 구성원 {}명 사진 {}장", family.getId(), familyMembers.size(), photoCount);

            // 결과 저장
            result.put(family.getId() + " Id", family.getId());
            result.put(family.getId() + " MembersCount", familyMembers.size());
            result.put(family.getId() + " Members", familyMembers.stream()
                    .map(m -> Map.of(
                            "uid", m.getUid(),
                            "name", m.getName(),
                            "email", m.getEmail(),
                            "role", m.getRole()))
                    .toList());
            result.put(family.getId() + " InviteCode", inviteCode);
        }

        return result;
    }

    /**
     * 고유한 사용자 ID를 생성합니다.
     *
     * @return 임의 생성된 UID
     */
    private String generateRandomUid(int length) {
        // 임의 UID 생성
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length + 4);
        sb.append("TEST");

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    private ReactionAddRequest generateRandomReaction() {
        String[] reactionTypes = {"LIKE", "LOVE", "STAR", "FIRE"};
        String randomReaction = reactionTypes[random.nextInt(reactionTypes.length)];
        int randomCount = random.nextInt(10) + 1;
        return new ReactionAddRequest(randomReaction, randomCount);
    }
}