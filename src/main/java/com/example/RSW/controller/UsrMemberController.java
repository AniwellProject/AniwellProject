package com.example.RSW.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.service.FirebaseService;
import com.example.RSW.service.NotificationService;
import com.example.RSW.service.VetCertificateService;
import com.example.RSW.vo.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import com.example.RSW.util.Ut;
import com.example.RSW.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Controller
public class UsrMemberController {

    // 카카오 REST API 키 주입
    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    // 카카오 리디렉트 URI 주입
    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    @Autowired
    private Rq rq;

    @Autowired
    private MemberService memberService;

    @Autowired
    private VetCertificateService vetCertificateService;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 허용 도메인
    private static final java.util.Set<String> ALLOWED_EMAIL_DOMAINS =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "naver.com", "gmail.com", "daum.net", "hanmail.net", "nate.com", "aniwell.com", "example.com"
            ));

    private static boolean isAllowedEmailDomain(String email) {
        if (email == null) return false;
        int at = email.lastIndexOf('@');
        if (at < 0) return false;
        String domain = email.substring(at + 1).toLowerCase();
        return ALLOWED_EMAIL_DOMAINS.contains(domain);
    }


    @RequestMapping("/usr/member/doLogout")
    public String doLogout(HttpServletRequest req) {

        Rq rq = (Rq) req.getAttribute("rq");

        rq.logout();

        return "redirect:/";
    }

    @RequestMapping("/usr/member/logout-complete")
    @ResponseBody
    public String logoutComplete(HttpServletRequest req, HttpServletResponse resp) {
        Rq rq = new Rq(req, resp, memberService);
        rq.logout();
        req.getSession().removeAttribute("kakaoAccessToken");  // 서버 세션, 토큰 삭제

        return """
                    <script>
                        if(window.opener) {
                            window.opener.postMessage("kakaoLogoutComplete", "*");
                            window.close();
                        } else {
                            location.href = "/";
                        }
                    </script>
                """;
    }


    @RequestMapping("/usr/member/service-logout-popup")
    @ResponseBody
    public String serviceLogoutPopup(HttpServletRequest req, HttpServletResponse resp) {
        // 세션 종료 로직
        Rq rq = new Rq(req, resp, memberService);
        rq.logout();

        // 디버깅용 로그 추가
        System.out.println("DEBUG: service-logout-popup 컨트롤러 호출됨");

        return """
                    <script>
                        if(window.opener) {
                            console.log('DEBUG: serviceLogoutComplete 메시지 전송');
                            window.opener.postMessage("serviceLogoutComplete", "*");
                            window.close();
                        } else {
                            location.href = "/";
                        }
                    </script>
                """;
    }


    @RequestMapping("/usr/member/login")
    public String showLogin(HttpServletRequest req, Model model) {

        model.addAttribute("kakaoRestApiKey", kakaoRestApiKey);
        model.addAttribute("kakaoRedirectUri", kakaoRedirectUri);

        return "/usr/member/login";
    }

    @RequestMapping("/usr/member/doLogin")
    @ResponseBody
    public ResultData doLogin(HttpServletRequest req, HttpServletResponse resp, String loginId, String loginPw,
                              @RequestParam(defaultValue = "/") String afterLoginUri) {

        Rq rq = (Rq) req.getAttribute("rq");              // ✅ 요청 스코프에서만 획득

        if (rq == null) {
            rq = new Rq(req, resp, memberService);
        }

        if (Ut.isEmptyOrNull(loginId)) {
            return ResultData.from("F-1", "아이디를 입력해주세요.");
        }
        if (Ut.isEmptyOrNull(loginPw)) {
            return ResultData.from("F-2", "비밀번호를 입력해주세요.");
        }

        Member member = memberService.getMemberByLoginId(loginId);

        if (member == null) {
            return ResultData.from("F-3", Ut.f("%s는(은) 존재하지 않는 아이디입니다.", loginId));
        }

        if (!member.getLoginPw().equals(Ut.sha256(loginPw))) {
            return ResultData.from("F-4", "비밀번호가 일치하지 않습니다.");
        }

        if (member.isDelStatus()) {
            return ResultData.from("F-5", "탈퇴한 회원입니다.");
        }

        // Spring Security 인증 등록
        CustomUserDetails userDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 세션에 Spring Security Context 저장
        req.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // 기존 rq.login 유지 (세션 기반 호환성)
        rq.login(member);

        // Firebase용 UID 기준 토큰 생성
        String uid = member.getLoginId() + "@aniwell.com";
        String firebaseToken = memberService.createFirebaseCustomToken(uid);
        req.getSession().setAttribute("firebaseToken", firebaseToken);

        // ✅ 추가: 일반 로그인 우선 잠금(소셜 토큰이 다시 세션을 덮지 못하게)
        req.getSession().setAttribute("authSource", "local");
        req.getSession().setAttribute("authSourceAt", System.currentTimeMillis());

        // 성공 응답 (JSON)
        Map<String, Object> data = new HashMap<>();
        data.put("token", firebaseToken);

        return ResultData.from("S-1", Ut.f("%s님 환영합니다", member.getNickname()), "data1", data);

    }


    @RequestMapping("/usr/member/join")
    public String showJoin(HttpServletRequest req) {
        return "/usr/member/join";
    }

    @RequestMapping("/usr/member/doJoin")
    @ResponseBody
    public String doJoin(HttpServletRequest req, String loginId, String loginPw, String name, String nickname,
                         String cellphone, String email, String address, String authName) {

        // 필수 입력값 체크
        if (Ut.isEmptyOrNull(loginId)) {
            return Ut.jsHistoryBack("F-1", "아이디를 입력하세요");
        }
        if (Ut.isEmptyOrNull(loginPw)) {
            return Ut.jsHistoryBack("F-2", "비밀번호를 입력하세요");
        }
        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-3", "이름을 입력하세요");
        }
        if (Ut.isEmptyOrNull(nickname)) {
            return Ut.jsHistoryBack("F-4", "닉네임을 입력하세요");
        }
        if (Ut.isEmptyOrNull(cellphone)) {
            return Ut.jsHistoryBack("F-5", "전화번호를 입력하세요");
        }
        if (Ut.isEmptyOrNull(email)) {
            return Ut.jsHistoryBack("F-6", "이메일을 입력하세요");
        }
        if (Ut.isEmptyOrNull(address)) {
            return Ut.jsHistoryBack("F-7", "주소를 입력하세요");
        }
        if (Ut.isEmptyOrNull(authName)) {
            return Ut.jsHistoryBack("F-8", "인증명을 입력하세요");
        }

        // 이메일 정규화 + 형식/도메인 체크
        String normEmail = normalizeEmail(email);
        if (!isValidEmail(normEmail) || !isAllowedEmailDomain(normEmail)) {
            return Ut.jsHistoryBack("F-6", "이메일 형식이 올바르지 않습니다.");
        }

        // 비밀번호 해시화
        String hashedLoginPw = Ut.sha256(loginPw);

        // 무조건 일반회원으로 가입
        int fixedAuthLevel = 1;

        // 회원가입 처리
        ResultData joinRd = memberService.join(loginId, hashedLoginPw, name, nickname, cellphone, normEmail, address, authName, fixedAuthLevel);

        if (joinRd.isFail()) {
            return Ut.jsHistoryBack(joinRd.getResultCode(), joinRd.getMsg());
        }

        // 성공 후 로그인 페이지로 리디렉션
        return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../member/login");
    }


    // 마이페이지
    @RequestMapping({"/usr/member/myPage", "/usr/member/mypage"})
    public String showMyPage(HttpServletRequest req, Model model) {

        Rq rq = (Rq) req.getAttribute("rq");
        Member loginedMember = rq.getLoginedMember();

        VetCertificate cert = vetCertificateService.getCertificateByMemberId(loginedMember.getId());
        model.addAttribute("cert", cert);

        model.addAttribute("member", loginedMember);

        return "usr/member/myPage";
    }

    @RequestMapping("/usr/member/checkPw")
    public String showCheckPw() {
        return "usr/member/checkPw";
    }

    @ResponseBody
    @RequestMapping("/usr/member/doCheckPw")
    public String doCheckPw(HttpServletRequest req, HttpServletResponse resp, String loginPw) throws IOException {
        Rq rq = (Rq) req.getAttribute("rq");

        // 소셜 로그인 회원은 비밀번호 확인 없이 바로 이동
        if (rq.getLoginedMember().isSocialMember()) {
            return "SOCIAL_OK";
        }

        // 일반 로그인 회원은 비밀번호 확인
        if (Ut.isEmptyOrNull(loginPw)) {
            return "비밀번호를 입력해 주세요.";
        }

        if (!rq.getLoginedMember().getLoginPw().equals(Ut.sha256(loginPw))) {
            return "비밀번호가 일치하지 않습니다.";

        }

        return "OK";
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static boolean isValidEmail(String email) {
        // 대부분 메일 서비스 커버 (한글/공백/잘못된 TLD 차단)
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    @RequestMapping("/usr/member/modify")
    public String showmyModify() {
        return "usr/member/modify";
    }

    @RequestMapping("/usr/member/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req,
                           @RequestParam(required = false) String loginPw,
                           @RequestParam String name,
                           @RequestParam String nickname,
                           @RequestParam String cellphone,
                           @RequestParam String email,
                           @RequestParam(required = false) MultipartFile photoFile,
                           @RequestParam String address) {

        long t0 = System.currentTimeMillis();
        Rq rq = (Rq) req.getAttribute("rq");

        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-3", "이름을 입력하세요.");
        }
        if (Ut.isEmptyOrNull(nickname)) {
            return Ut.jsHistoryBack("F-4", "닉네임을 입력하세요.");
        }
        if (Ut.isEmptyOrNull(cellphone)) {
            return Ut.jsHistoryBack("F-5", "전화번호를 입력하세요.");
        }
        if (Ut.isEmptyOrNull(email)) {
            return Ut.jsHistoryBack("F-6", "이메일을 입력하세요.");
        }
        if (Ut.isEmptyOrNull(address)) {
            return Ut.jsHistoryBack("F-7", "주소를 입력하세요.");
        }

        // 정규화
        String normCellphone = cellphone.replaceAll("\\D", "");
        String normEmail = normalizeEmail(email);

        boolean isKakaoPlaceholder = normEmail != null && normEmail.endsWith("@noemail.kakao");

        if (!isKakaoPlaceholder) {
            boolean emailValid = isValidEmail(normEmail);
            boolean domainAllowed = isAllowedEmailDomain(normEmail);
            if (!emailValid || !domainAllowed) {
                return Ut.jsHistoryBack("F-6", "이메일 형식이 올바르지 않습니다.");
            }
        }

        boolean phoneRegexOk = normCellphone.matches("^(010\\d{8}|01[16789]\\d{7})$");
        if (!phoneRegexOk) {
            return Ut.jsHistoryBack("F-5", "전화번호 형식이 올바르지 않습니다.");
        }

        int memberId = rq.getLoginedMemberId();

        // 중복 체크
        Member emailOwner = memberService.getMemberByEmail(normEmail);
        if (emailOwner != null && emailOwner.getId() != memberId) {
            return Ut.jsHistoryBack("F-6", "이미 사용 중인 이메일입니다.");
        }

        Member nicknameOwner = memberService.getMemberByNickname(nickname);
        if (nicknameOwner != null && nicknameOwner.getId() != memberId) {
            return Ut.jsHistoryBack("F-4", "이미 사용 중인 닉네임입니다.");
        }

        Member phoneOwner = memberService.getMemberByCellphone(normCellphone);
        if (phoneOwner != null && phoneOwner.getId() != memberId) {
            return Ut.jsHistoryBack("F-5", "이미 사용 중인 전화번호입니다.");
        }

        String photoUrl = null;
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                long up0 = System.currentTimeMillis();
                Map uploadResult = cloudinary.uploader().upload(photoFile.getBytes(), ObjectUtils.emptyMap());
                photoUrl = (String) uploadResult.get("secure_url");
            } catch (IOException e) {
                return Ut.jsHistoryBack("F-7", "사진 업로드 실패: " + e.getMessage());
            }
        }

        // 수정 호출
        ResultData modifyRd;
        if (Ut.isEmptyOrNull(loginPw)) {
            modifyRd = memberService.modifyWithoutPw(memberId, name, nickname, normCellphone, normEmail, photoUrl, address);
        } else {
            modifyRd = memberService.modify(memberId, loginPw, name, nickname, normCellphone, normEmail, photoUrl, address);
        }

        Member updatedMember = memberService.getMemberById(memberId);

        rq.setLoginedMember(updatedMember);

        return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../member/myPage");
    }


    @RequestMapping("/usr/member/getLoginIdDup")
    @ResponseBody
    public ResultData getLoginIdDup(String loginId) {

        if (Ut.isEmpty(loginId)) {
            return ResultData.from("F-1", "아이디를 입력해주세요");
        }

        Member existsMember = memberService.getMemberByLoginId(loginId);

        if (existsMember != null) {
            return ResultData.from("F-2", "해당 아이디는 이미 사용중입니다.", "loginId", loginId);
        }

        return ResultData.from("S-1", "사용 가능한 아이디입니다.", "loginId", loginId);
    }

    @RequestMapping("/usr/member/getEmailDup")
    @ResponseBody
    public ResultData getEmailDup(HttpServletRequest req, @RequestParam String email) {

        if (Ut.isEmpty(email)) {
            return ResultData.from("F-1", "이메일을 입력해주세요");
        }

        // ✅ 정규화 + 형식검증
        String normEmail = normalizeEmail(email);
        if (!isValidEmail(normEmail)) {
            return ResultData.from("F-3", "이메일 형식이 올바르지 않습니다.", "email", normEmail);
        }

        if (!isAllowedEmailDomain(normEmail)) {
            return ResultData.from("F-3", "이메일 형식이 올바르지 않습니다.", "email", normEmail);
        }

        Member existsMember = memberService.getMemberByEmail(normEmail); // ← 서비스/레포는 소문자 비교 권장

        // 본인 제외 (수정 화면에서 자기 이메일이면 사용 가능 처리)
        Rq rq = (Rq) req.getAttribute("rq");
        Integer meId = (rq != null && rq.isLogined()) ? rq.getLoginedMemberId() : null;

        if (existsMember != null && (meId == null || existsMember.getId() != meId)) {
            return ResultData.from("F-2", "이미 사용 중인 이메일입니다.", "email", normEmail);
        }

        return ResultData.from("S-1", "사용 가능한 이메일입니다.", "email", normEmail);
    }

    @RequestMapping("/usr/member/getNicknameDup")
    @ResponseBody
    public ResultData getNicknameDup(String nickname) {
        if (Ut.isEmpty(nickname)) {
            return ResultData.from("F-1", "닉네임을 입력해주세요");
        }

        Member existsMember = memberService.getMemberByNickname(nickname);

        if (existsMember != null) {
            return ResultData.from("F-2", "이미 사용 중인 닉네임입니다.", "nickname", nickname);
        }

        return ResultData.from("S-1", "사용 가능한 닉네임입니다.", "nickname", nickname);
    }

    @RequestMapping("/usr/member/getCellphoneDup")
    @ResponseBody
    public ResultData getCellphoneDup(String cellphone) {
        if (Ut.isEmpty(cellphone)) {
            return ResultData.from("F-1", "전화번호를 입력해주세요.");
        }

        // 숫자만 남김 (하이픈, 공백 등 제거)
        String digits = cellphone.replaceAll("\\D", "");

        // 서버에서도 한 번 검증
        if (!digits.matches("^(010\\d{8}|01[16789]\\d{7})$")) {
            return ResultData.from("F-3", "전화번호 형식이 올바르지 않습니다.", "cellphone", digits);
        }

        Member existsMember = memberService.getMemberByCellphone(digits);

        if (existsMember != null) {
            return ResultData.from("F-2", "이미 사용 중인 전화번호입니다.", "cellphone", digits);
        }

        return ResultData.from("S-1", "사용 가능한 전화번호입니다.", "cellphone", digits);
    }

    @RequestMapping("/usr/member/findLoginId")
    public String showFindLoginId() {

        return "usr/member/findLoginId";
    }

    @RequestMapping("/usr/member/doFindLoginId")
    @ResponseBody
    public ResultData doFindLoginId(@RequestParam(defaultValue = "/usr/member/login") String afterFindLoginIdUri,
                                    @RequestParam("name") String name, @RequestParam("email") String email) {

        Member member = memberService.getMemberByNameAndEmail(name, normalizeEmail(email)); // 정규화 전달

        if (member == null) {
            return ResultData.from("F-1", "해당하는 아이디가 없습니다.");
        }

        return ResultData.from("S-1", "아이디 찾기 성공", "data1", member.getLoginId());
    }


    @RequestMapping("/usr/member/findLoginPw")
    public String showFindLoginPw() {

        return "usr/member/findLoginPw";
    }

    @RequestMapping("/usr/member/doFindLoginPw")
    @ResponseBody
    public ResultData doFindLoginPw(@RequestParam(defaultValue = "/") String afterFindLoginPwUri, String loginId,
                                    String email) {

        Member member = memberService.getMemberByLoginId(loginId);

        if (member == null) {
            return ResultData.from("F-1", "존재하지 않는 회원입니다.");
        }

        String normEmail = normalizeEmail(email);
        if (!normalizeEmail(member.getEmail()).equals(normEmail)) { // 정규화 후 비교
            return ResultData.from("F-2", "일치하는 이메일이 없습니다.");
        }

        ResultData
                notifyTempLoginPwByEmailRd = memberService.notifyTempLoginPwByEmail(member);

        return ResultData.from(notifyTempLoginPwByEmailRd.getResultCode(), notifyTempLoginPwByEmailRd.getMsg(),
                afterFindLoginPwUri);
    }

    @RequestMapping("/usr/member/doWithdraw")
    @ResponseBody
    public ResultData<?> doWithdraw(HttpServletRequest req, HttpServletResponse resp) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (!rq.isLogined()) {
            return ResultData.from("F-1", "로그인 후 이용해주세요.");
        }


        Member member = rq.getLoginedMember();

        // 소셜회원인지 확인
        if (member.isSocialMember() && "kakao".equals(member.getSocialProvider())) {
            String kakaoAccessToken = (String) req.getSession().getAttribute("kakaoAccessToken");

            if (kakaoAccessToken != null) {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + kakaoAccessToken);
                    HttpEntity<?> entity = new HttpEntity<>(headers);

                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            "https://kapi.kakao.com/v1/user/unlink", entity, Map.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        System.out.println("✅ 카카오 연결 해제 성공");
                    } else {
                        System.out.println("⚠ 카카오 unlink 실패: " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    System.out.println("❌ 카카오 unlink 예외: " + e.getMessage());
                }

                req.getSession().removeAttribute("kakaoAccessToken");
            }
        }

        // 서비스 회원 탈퇴 처리
        memberService.withdrawMember(member.getId());

        // 로그아웃
        rq.logout();

        return ResultData.from("S-1", "회원 탈퇴가 완료되었습니다.");
    }


    @RequestMapping("/usr/member/vetCert")
    public String showVetCertForm(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");

        // 수의사 신청자인지 확인
        if (!"수의사".equals(rq.getLoginedMember().getAuthName())) {
            model.addAttribute("errorMsg", "수의사만 인증서 제출이 가능합니다.");
            return "common/error";
        }

        return "usr/member/vetCertUpload"; // JSP 경로
    }

    @RequestMapping("/usr/member/doVetCertUpload")
    @ResponseBody
    public String doVetCertUpload(HttpServletRequest req, @RequestParam("file") MultipartFile file) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (file.isEmpty()) {
            return Ut.jsReplace("F-1", "❗ 파일을 선택해주세요.", "/usr/member/myPage");
        }

        try {
            // 기존 인증서 삭제
            VetCertificate existing = vetCertificateService.getCertificateByMemberId(rq.getLoginedMemberId());
            if (existing != null) {
                vetCertificateService.deleteCertificateWithFile(existing); // 기존 DB 삭제
                // Cloudinary도 삭제하고 싶으면 이후 추가
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return Ut.jsReplace("F-2", "파일명이 유효하지 않습니다.", "/usr/member/myPage");
            }

            // ✅ Cloudinary 업로드
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String secureUrl = (String) uploadResult.get("secure_url");

            VetCertificate cert = new VetCertificate();
            cert.setMemberId(rq.getLoginedMemberId());
            cert.setFileName(originalFilename);
            cert.setFilePath(secureUrl); // 🔄 실제 저장은 Cloudinary의 URL
            cert.setUploadedAt(LocalDateTime.now());
            cert.setApproved(0);

            System.out.println("📤 Cloudinary 인증서 업로드됨: " + secureUrl);

            vetCertificateService.registerCertificate(cert);
            memberService.updateVetCertInfo(rq.getLoginedMemberId(), secureUrl, 0);


            // 관리자 알림 전송
            notificationService.sendNotificationToAdmins(rq.getLoginedMemberId());

            return """
                        <html>
                        <head><meta charset="UTF-8"><script>
                        alert('✅ 수의사 인증서가 등록되었습니다. 관리자 승인을 기다려주세요.');
                        location.replace('myCert');
                        </script></head><body></body></html>
                    """;

        } catch (Exception e) {
            e.printStackTrace();
            return """
                        <html>
                        <head><meta charset="UTF-8"><script>
                        alert('⚠ 업로드 중 오류가 발생했습니다. 다시 시도해주세요.');
                        location.replace('/usr/member/myPage');
                        </script></head><body></body></html>
                    """;
        }
    }


    @RequestMapping("/usr/member/myCert")
    public String showMyCertificate(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");

        VetCertificate cert = vetCertificateService.getCertificateByMemberId(rq.getLoginedMemberId());

        model.addAttribute("cert", cert);
        return "usr/member/myCert";
    }

    @RequestMapping("/usr/member/deleteVetCert")
    @ResponseBody
    public String deleteVetCert(HttpServletRequest req) {
        Rq rq = (Rq) req.getAttribute("rq");

        VetCertificate cert = vetCertificateService.getCertificateByMemberId(rq.getLoginedMemberId());

        if (cert == null) {
            return Ut.jsHistoryBack("F-1", "삭제할 인증서가 없습니다.");
        }

        vetCertificateService.deleteCertificateWithFile(cert);

        return Ut.jsReplace("S-1", "인증서가 삭제되었습니다.", "/usr/member/myCert");
    }

    // ✅ 카카오 로그인
    @RequestMapping("/usr/member/kakao")
    public void kakaoPopupCallback(@RequestParam("code") String code,
                                   HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            String tokenUrl = "https://kauth.kakao.com/oauth/token";
            RestTemplate restTemplate = new RestTemplate();

            // 1️⃣ Access Token 발급
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("client_id", kakaoRestApiKey);
            tokenParams.add("redirect_uri", kakaoRedirectUri);
            tokenParams.add("client_secret", kakaoClientSecret);
            tokenParams.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 2️⃣ 사용자 정보 요청
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<?> profileRequest = new HttpEntity<>(profileHeaders);

            ResponseEntity<Map> profileResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    profileRequest,
                    Map.class
            );

            Map body = profileResponse.getBody();
            Map properties = (Map) body.get("properties");
            String socialId = String.valueOf(body.get("id"));
            String name = (String) properties.get("nickname");
            String provider = "kakao";

            // ✅ 이메일 강제 생성
            String email = provider + "_" + socialId + "@noemail.kakao";

            // 3️⃣ DB 등록/로그인
            Member member = memberService.getOrCreateSocialMember(provider, socialId, email, name);

            // 4️⃣ 세션 등록
            Rq rq = new Rq(req, resp, memberService);
            rq.login(member);
            req.getSession().setAttribute("kakaoAccessToken", accessToken);

            // ✅ Spring Security 인증 등록
            CustomUserDetails userDetails = new CustomUserDetails(member);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            req.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // ✅ Firebase 토큰 생성 및 Redis 7일 저장
            String uid = member.getUid(); // ← 서비스에서 이미 uid 저장 & 재조회됨
            String firebaseToken = memberService.createFirebaseCustomToken(uid);
            req.getSession().setAttribute("firebaseToken", firebaseToken);
            redisTemplate.opsForValue().set("firebase:token:" + uid, firebaseToken, 7, TimeUnit.DAYS);

            // 6️⃣ 부모창으로 이메일 전달
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<script>");
            out.println("localStorage.setItem('kakaoAccessToken', '" + accessToken + "');");
            out.println("window.opener.postMessage({ email: '" + email + "' }, '*');");
            out.println("window.close();");
            out.println("</script>");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ [ERROR] kakaoPopupCallback 예외 발생: " + e.getMessage());
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<script>alert('카카오 로그인 중 오류 발생'); window.close();</script>");
        }
    }


    // 카카오 팝업 로그인 처리용 REST API 컨트롤러 메서드
    @PostMapping("/usr/member/social-login")
    @ResponseBody
    public ResultData<?> kakaoSocialLogin(@RequestBody Map<String, Object> payload,
                                          HttpServletRequest req, HttpServletResponse resp) {

        String provider = (String) payload.get("provider"); // "kakao"
        String socialId = String.valueOf(payload.get("socialId"));
        String name = (String) payload.get("name");
        String email = (String) payload.get("email");

        Member member = memberService.getOrCreateSocialMember(provider, socialId, email, name);

        Rq rq = new Rq(req, resp, memberService);
        rq.login(member);

        return ResultData.from("S-1", "로그인 성공");
    }

    @RequestMapping("/usr/member/kakao-popup-login")
    public void kakaoPopupRedirect(@RequestParam(value = "token", required = false) String accessTokenParam, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String accessToken = accessTokenParam != null
                ? accessTokenParam
                : (String) req.getSession().getAttribute("kakaoAccessToken");

        if (accessToken != null) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                Map properties = (Map) response.getBody().get("properties");
                String socialId = String.valueOf(response.getBody().get("id"));
                String name = (String) properties.get("nickname");

                Member member = memberService.getOrCreateSocialMember("kakao", socialId, "", name);

                Rq rq = new Rq(req, resp, memberService);
                rq.login(member);

                resp.setContentType("text/html; charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println("<script>window.opener.location.href = '/'; window.close();</script>");
                return;

            } catch (Exception e) {
                // access_token 만료됐을 때
                req.getSession().removeAttribute("kakaoAccessToken");
            }
        }
        String clientId = "79f2a3a73883a82595a2202187f96cc5";
        String redirectUri = "http://localhost:8080/usr/member/kakao";
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&prompt=login";

        resp.sendRedirect(kakaoAuthUrl);
    }

    @PostMapping("/usr/member/kakao-popup-login")
    @ResponseBody
    public ResponseEntity<?> kakaoPopupLogin(@RequestBody Map<String, String> body,
                                             HttpServletRequest req, HttpServletResponse resp) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Missing token");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map properties = (Map) response.getBody().get("properties");
            String socialId = String.valueOf(response.getBody().get("id"));
            String name = (String) properties.get("nickname");

            Member member = memberService.getOrCreateSocialMember("kakao", socialId, "", name);

            Rq rq = new Rq(req, resp, memberService);
            rq.login(member);

            return ResponseEntity.ok("자동 로그인 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }
    }

    // ✅ 구글 로그인
    @RequestMapping("/usr/member/google")
    public void googleCallback(@RequestParam("code") String code,
                               HttpServletRequest req,
                               HttpServletResponse resp) {

        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1️⃣ 토큰 발급
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", "");
            params.add("client_secret", "");
            params.add("redirect_uri", "http://localhost:8080/usr/member/google");
            params.add("grant_type", "authorization_code");

            Map<String, Object> tokenResponse = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token", params, Map.class
            );


            if (tokenResponse == null || tokenResponse.get("access_token") == null) {
                resp.setContentType("text/html; charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println("<script>alert('구글 토큰 발급 실패'); window.close();</script>");
                return;
            }

            String accessToken = (String) tokenResponse.get("access_token");

            // 2️⃣ 사용자 정보 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> userInfo = userInfoResponse.getBody();
            if (userInfo == null) {
                resp.setContentType("text/html; charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println("<script>alert('구글 사용자 정보 조회 실패'); window.close();</script>");
                return;
            }

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String socialId = (String) userInfo.get("id");

            // 3️⃣ 회원 생성/조회
            Member member = memberService.getOrCreateSocialMember("google", socialId, email, name);

            // 4️⃣ Firebase 토큰 발급 및 Redis 7일 캐싱
            String uid = member.getUid(); // DB 저장된 uid
            String firebaseToken = memberService.createFirebaseCustomToken(uid);

            try {
                redisTemplate.opsForValue().set("firebase:token:" + uid, firebaseToken, 7, java.util.concurrent.TimeUnit.DAYS);
            } catch (Exception re) {
            }

            // 5️⃣ Spring Security 등록 + 세션 저장
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(member, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            req.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            req.getSession().setAttribute("loginedMemberId", member.getId());
            req.getSession().setAttribute("loginedMember", member);
            req.getSession().setAttribute("firebaseToken", firebaseToken);

            Rq rq = new Rq(req, resp, memberService);
            rq.login(member);

            // 6️⃣ 부모창 메시지
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<script>");
            out.println("window.opener.postMessage('socialLoginSuccess', '*');");
            out.println("window.close();");
            out.println("</script>");
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                resp.setContentType("text/html; charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println("<script>alert('구글 로그인 실패'); window.close();</script>");
                out.flush();
            } catch (Exception ignored) {
            }
        }
    }


    // ✅ 네이버 로그인 (Firebase + Redis 7일)
    @RequestMapping("/usr/member/naver")
    @ResponseBody
    public String naverCallback(@RequestParam("code") String code,
                                @RequestParam("state") String state,
                                HttpServletRequest req, HttpServletResponse resp) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1️⃣ 토큰 발급
            String tokenUrl = "https://nid.naver.com/oauth2.0/token"
                    + "?grant_type=authorization_code"
                    + "&client_id=" + "ZdyW5GGtNSgCCaduup7_"
                    + "&client_secret=" + "pJh4IlGi2_"
                    + "&code=" + code
                    + "&state=" + state;

            ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(tokenUrl, Map.class);
            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 2️⃣ 사용자 정보 요청
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me", HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class);

            Map<String, Object> response = (Map<String, Object>) userInfoResponse.getBody().get("response");
            String socialId = String.valueOf(response.get("id"));
            String name = (String) response.get("name");
            String email = (String) response.get("email");

            // 3️⃣ DB 등록/로그인
            Member member = memberService.getOrCreateSocialMember("naver", socialId, email, name);

            // 4️⃣ Firebase 토큰 생성 및 Redis 7일 저장
            String uid = member.getUid();
            String firebaseToken = memberService.createFirebaseCustomToken(uid);
            redisTemplate.opsForValue().set("firebase:token:" + uid, firebaseToken, 7, TimeUnit.DAYS);

            // 5️⃣ Spring Security 세션 등록
            CustomUserDetails userDetails = new CustomUserDetails(member);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            req.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            req.getSession().setAttribute("firebaseToken", firebaseToken);

            return "<script>window.opener.postMessage({ email: '" + email + "' }, '*'); window.close();</script>";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/usr/member/login?error=naver";
        }
    }


    // ✅ Firebase Custom Token 발급 (Redis 캐싱)
    @RequestMapping("/usr/member/firebase-token")
    @ResponseBody
    public ResultData<Map<String, String>> generateFirebaseToken(HttpServletRequest req) {
        Integer memberId = (Integer) req.getSession().getAttribute("loginedMemberId");

        if (memberId == null) return ResultData.from("F-1", "로그인 후 이용 가능합니다.");

        Member loginedMember = memberService.getMemberById(memberId);
        if (loginedMember == null) return ResultData.from("F-2", "회원 정보를 찾을 수 없습니다.");

        try {
            String customToken = memberService.getOrCreateFirebaseToken(loginedMember);

            Map<String, String> data = new HashMap<>();
            data.put("token", customToken);

            return ResultData.from("S-1", "토큰 생성 성공", data);
        } catch (RuntimeException e) {
            return ResultData.from("F-3", "토큰 생성 실패: " + e.getMessage());
        }
    }


    // ✅ Firebase 세션 로그인 (Redis 기반 최적화)
    @RequestMapping("/usr/member/firebase-session-login")
    @ResponseBody
    public ResultData doFirebaseSessionLogin(@RequestBody Map<String, String> body, HttpServletRequest req) {

        String idToken = body.get("idToken");

        // 0) 이미 소셜(네이버/카카오)로 확정된 세션이면 덮지 않음
        Object authSource = req.getSession().getAttribute("authSource");
        if ("social-fixed".equals(authSource)) {
            return ResultData.from("S-2", "세션 유지: 소셜 우선");
        }

        // 1) 구글 플래그 확인(구글 경로에서만 진행)
        Boolean allow = (Boolean) req.getSession().getAttribute("allowFirebaseLogin");
        String expectProvider = (String) req.getSession().getAttribute("expectProvider");
        if (!Boolean.TRUE.equals(allow) || !"google".equals(expectProvider)) {
            return ResultData.from("S-0", "무시됨: 구글 절차 아님");
        }

        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decoded.getUid();

            // 구글은 sign_in_provider가 google 또는 custom 모두 허용
            String prov = memberService.extractProvider(decoded); // null/custom/google 가능
            if (prov != null && !"google".equals(prov) && !"custom".equals(prov)) {
                clearGoogleFlags(req);
                return ResultData.from("S-0", "무시됨: 구글 토큰 아님");
            }

            // 구글 프로필 동기화(이름/닉네임/사진) + provider='google'
            Member member = memberService.findByUid(uid);
            if (member == null) {
                member = memberService.syncGoogleProfileFromFirebaseToken(decoded);
                if (member == null) {
                    clearGoogleFlags(req);
                    return ResultData.from("F-9", "회원 정보를 찾을 수 없습니다.");
                }
            } else {
                // 보수적 동기화
                member = memberService.syncGoogleProfileFromFirebaseToken(decoded);
                if (member == null) member = memberService.findByUid(uid);
            }

            setSpringSecuritySession(req, member);
            clearGoogleFlags(req);

            return ResultData.from("S-1", "구글 로그인 완료");

        } catch (FirebaseAuthException e) {
            clearGoogleFlags(req);
            return ResultData.from("F-1", "Firebase 인증 실패: " + e.getMessage());
        } catch (Exception e) {
            clearGoogleFlags(req);
            return ResultData.from("F-2", "로그인 처리 중 오류");
        }
    }

    private void clearGoogleFlags(HttpServletRequest req) {
        req.getSession().removeAttribute("allowFirebaseLogin");
        req.getSession().removeAttribute("expectProvider");
    }

    // ✅ Spring Security 세션 설정 메서드
    private void setSpringSecuritySession(HttpServletRequest req, Member member) {
        req.getSession().setAttribute("loginedMemberId", member.getId());
        req.getSession().setAttribute("loginedMember", member);
        CustomUserDetails userDetails = new CustomUserDetails(member);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        req.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    }

    // ✅ 소셜 로그인
    @RequestMapping("/usr/member/social-login")
    @ResponseBody
    public ResultData socialLogin(@RequestParam String email,
                                  @RequestParam(required = false) String name,
                                  HttpServletRequest req) {

        String lower = email == null ? "" : email.toLowerCase();
        boolean isGoogle = lower.endsWith("@gmail.com") || lower.endsWith("@googlemail.com");
        boolean isKakao  = lower.endsWith("@noemail.kakao") || lower.startsWith("kakao_");
        boolean isNaver  = lower.endsWith("@naver.com");

        Member member = memberService.findByEmail(email);

        // 네이버/카카오: 여기서 바로 세션 확정 + 리디렉트용 성공 응답
        if (isKakao || isNaver) {
            if (member == null) {
                return ResultData.from("F-1", "해당 이메일의 회원이 없습니다.");
            }
            // 세션/시큐리티 확정
            setSpringSecuritySession(req, member);
            // 이후 firebase-session-login이 와도 절대 덮지 않도록 락
            req.getSession().setAttribute("authSource", "social-fixed");

            return ResultData.from("S-1", "소셜 로그인 완료(네이버/카카오)");
        }

        // 구글: 기존 흐름(커스텀 토큰 발급) 유지 + 구글 전용 플래그로만 다음 단계 허용
        if (member == null) {
            member = memberService.getOrCreateByEmail(email, (name != null ? name : "구글사용자"), "google");
        }
        req.getSession().setAttribute("allowFirebaseLogin", Boolean.TRUE);
        req.getSession().setAttribute("expectProvider", "google");
        req.getSession().removeAttribute("authSource"); // 구글은 락 없음

        String redisKey = "firebase:token:" + member.getUid();
        try {
            String cached = redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                return ResultData.from("S-1", "캐시된 토큰 사용", "data1", cached, "provider", "google");
            }
        } catch (Exception ignore) {}

        String firebaseToken = firebaseService.createCustomToken(member);
        try { redisTemplate.opsForValue().set(redisKey, firebaseToken, 12, TimeUnit.HOURS); } catch (Exception ignore) {}

        return ResultData.from("S-1", "새 토큰 발급", "data1", firebaseToken, "provider", "google");
    }

}