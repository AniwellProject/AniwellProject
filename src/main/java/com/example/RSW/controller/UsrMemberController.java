package com.example.RSW.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.service.NotificationService;
import com.example.RSW.service.VetCertificateService;
import com.example.RSW.vo.VetCertificate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
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
import java.util.Map;
import java.util.UUID;

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

        Rq rq = (Rq) req.getSession().getAttribute("rq");

        if (rq == null) {
            rq = new Rq(req, resp, memberService);
            req.getSession().setAttribute("rq", rq);
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

        // 로그인 처리
        rq.login(member);
        req.getSession().setAttribute("rq", rq);

        // Firebase용 UID 기준 토큰 생성 (선택사항: uid = 이메일도 가능)
        String uid = member.getLoginId() + "@aniwell.com";
        String firebaseToken = memberService.createFirebaseCustomToken(uid); // 너가 만든 메서드
        req.getSession().setAttribute("firebaseToken", firebaseToken);

        System.out.println("✅ [로그] 로그인 성공 → memberId: " + member.getId());
        // 성공 응답 (JSON)
        return ResultData.from("S-1", Ut.f("%s님 환영합니다", member.getNickname()));
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
            return Ut.jsHistoryBack("F-1", "아이디를 입력해");
        }
        if (Ut.isEmptyOrNull(loginPw)) {
            return Ut.jsHistoryBack("F-2", "비밀번호를 입력해");
        }
        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-3", "이름을 입력해");
        }
        if (Ut.isEmptyOrNull(nickname)) {
            return Ut.jsHistoryBack("F-4", "닉네임을 입력해");
        }
        if (Ut.isEmptyOrNull(cellphone)) {
            return Ut.jsHistoryBack("F-5", "전화번호를 입력해");
        }
        if (Ut.isEmptyOrNull(email)) {
            return Ut.jsHistoryBack("F-6", "이메일을 입력해");
        }
        if (Ut.isEmptyOrNull(address)) {
            return Ut.jsHistoryBack("F-7", "주소를 입력해");
        }
        if (Ut.isEmptyOrNull(authName)) {
            return Ut.jsHistoryBack("F-8", "인증명을 입력해");
        }

        // 비밀번호 해시화
        String hashedLoginPw = Ut.sha256(loginPw);

        // 무조건 일반회원으로 가입
        int fixedAuthLevel = 1;

        // 회원가입 처리
        ResultData joinRd = memberService.join(loginId, hashedLoginPw, name, nickname, cellphone, email, address, authName, fixedAuthLevel);

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

    @RequestMapping("/usr/member/doCheckPw")
    public void doCheckPw(HttpServletRequest req, HttpServletResponse resp, String loginPw) throws IOException {
        Rq rq = (Rq) req.getAttribute("rq");

        // 소셜 로그인 회원은 비밀번호 확인 없이 바로 이동
        if (rq.getLoginedMember().isSocialMember()) {
            resp.sendRedirect("modify");
            return;
        }

        // 일반 로그인 회원은 비밀번호 확인
        if (Ut.isEmptyOrNull(loginPw)) {
            rq.printHistoryBack("비밀번호를 입력해 주세요.");
            return;
        }

        if (!rq.getLoginedMember().getLoginPw().equals(Ut.sha256(loginPw))) {
            rq.printHistoryBack("비밀번호가 일치하지 않습니다.");
            return;
        }

        // 성공 시 수정 페이지로 리다이렉트
        resp.sendRedirect("modify");
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

        Rq rq = (Rq) req.getAttribute("rq");

        if (Ut.isEmptyOrNull(name)) return Ut.jsHistoryBack("F-3", "이름을 입력하세요.");
        if (Ut.isEmptyOrNull(nickname)) return Ut.jsHistoryBack("F-4", "닉네임을 입력하세요.");
        if (Ut.isEmptyOrNull(cellphone)) return Ut.jsHistoryBack("F-5", "전화번호를 입력하세요.");
        if (Ut.isEmptyOrNull(email)) return Ut.jsHistoryBack("F-6", "이메일을 입력하세요.");

        String photoUrl = null;

        // 1단계: 업로드 파일 확인
        System.out.println("📸 업로드된 파일: " + (photoFile != null ? photoFile.getOriginalFilename() : "파일 없음"));

        // 2단계: 클라우디너리 업로드
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                System.out.println("📤 Cloudinary 업로드 시작");
                Map uploadResult = cloudinary.uploader().upload(photoFile.getBytes(), ObjectUtils.emptyMap());
                photoUrl = (String) uploadResult.get("secure_url");
                System.out.println("✅ Cloudinary 업로드 완료: " + photoUrl);
            } catch (IOException e) {
                System.out.println("❌ Cloudinary 업로드 실패: " + e.getMessage());
                return Ut.jsHistoryBack("F-7", "사진 업로드 실패: " + e.getMessage());
            }
        }

        // 3단계: 서비스 호출
        int memberId = rq.getLoginedMemberId();

        System.out.println("📝 전달할 회원정보");
        System.out.println("이름: " + name);
        System.out.println("닉네임: " + nickname);
        System.out.println("전화번호: " + cellphone);
        System.out.println("이메일: " + email);
        System.out.println("비밀번호 있음?: " + (loginPw != null && !loginPw.isBlank()));
        System.out.println("사진 URL: " + photoUrl);

        ResultData modifyRd;
        if (Ut.isEmptyOrNull(loginPw)) {
            modifyRd = memberService.modifyWithoutPw(memberId, name, nickname, cellphone, email, photoUrl, address);
        } else {
            modifyRd = memberService.modify(memberId, loginPw, name, nickname, cellphone, email, photoUrl);
        }

        // 4단계: 세션 최신화
        Member updatedMember = memberService.getMemberById(memberId);
        rq.setLoginedMember(updatedMember);
        System.out.println("🧩 세션 로그인 사용자 갱신 완료");

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
            return ResultData.from("F-2", "해당 아이디는 이미 사용중이야", "loginId", loginId);
        }

        return ResultData.from("S-1", "사용 가능!", "loginId", loginId);
    }

    @RequestMapping("/usr/member/findLoginId")
    public String showFindLoginId() {

        return "usr/member/findLoginId";
    }

    @RequestMapping("/usr/member/doFindLoginId")
    @ResponseBody
    public String doFindLoginId(@RequestParam(defaultValue = "/usr/member/login") String afterFindLoginIdUri,
                                String name, String email) {

        Member member = memberService.getMemberByNameAndEmail(name, email);

        if (member == null) {
            return Ut.jsHistoryBack("F-1", "너는 없는 사람이야");
        }

        return Ut.jsReplace("S-1", Ut.f("너의 아이디는 [ %s ] 야", member.getLoginId()), afterFindLoginIdUri);
    }


    @RequestMapping("/usr/member/findLoginPw")
    public String showFindLoginPw() {

        return "usr/member/findLoginPw";
    }

    @RequestMapping("/usr/member/doFindLoginPw")
    @ResponseBody
    public String doFindLoginPw(@RequestParam(defaultValue = "/") String afterFindLoginPwUri, String loginId,
                                String email) {

        Member member = memberService.getMemberByLoginId(loginId);

        if (member == null) {
            return Ut.jsHistoryBack("F-1", "너는 없는 사람이야");
        }

        if (member.getEmail().equals(email) == false) {
            return Ut.jsHistoryBack("F-2", "일치하는 이메일이 없는데?");
        }

        ResultData notifyTempLoginPwByEmailRd = memberService.notifyTempLoginPwByEmail(member);

        return Ut.jsReplace(notifyTempLoginPwByEmailRd.getResultCode(), notifyTempLoginPwByEmailRd.getMsg(),
                afterFindLoginPwUri);
    }

    @RequestMapping("/usr/member/doWithdraw")
    @ResponseBody

    public String doWithdraw(HttpServletRequest req, HttpServletResponse resp) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (!rq.isLogined()) {
            return Ut.jsHistoryBack("F-1", "로그인 후 이용해주세요.");
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

        return Ut.jsReplace("S-1", "회원 탈퇴가 완료되었습니다.", "/");
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

    // 카카오 로그인
    @RequestMapping("/usr/member/kakao")
    public void kakaoPopupCallback(@RequestParam("code") String code,
                                   HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", kakaoRestApiKey);
        tokenParams.add("redirect_uri", "http://localhost:8080/usr/member/kakao");
        tokenParams.add("client_secret", kakaoClientSecret);
        tokenParams.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

        String accessToken = (String) tokenResponse.getBody().get("access_token");

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
        Map kakaoAccount = (Map) body.get("kakao_account");

        String socialId = String.valueOf(body.get("id"));
        String name = (String) properties.get("nickname");
        String provider = "kakao";

        // ✅ 이메일 강제 생성
        String email = provider + "_" + socialId + "@noemail.kakao";
        System.out.println("📌 강제 생성된 email: " + email);


        // 기존 사용자 조회 또는 새로 생성
        Member member = memberService.getOrCreateSocialMember(provider, socialId, email, name);

        // 세션 등록
        Rq rq = new Rq(req, resp, memberService);
        rq.login(member);
        req.getSession().setAttribute("rq", rq);
        req.getSession().setAttribute("kakaoAccessToken", accessToken);

        // ✅ Firebase 토큰 생성 및 세션 저장
        String uid = member.getSocialProvider() + "_" + member.getSocialId();
        String firebaseToken = memberService.createFirebaseCustomToken(uid);
        req.getSession().setAttribute("firebaseToken", firebaseToken);

        // ✅ 팝업 닫고 부모 창에 Firebase 연동 메시지 전송
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<script>");
        out.println("localStorage.setItem('kakaoAccessToken', '" + accessToken + "');");
        out.println("window.opener.postMessage('socialLoginSuccess', '*');");
        out.println("window.close();");
        out.println("</script>");
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
        req.getSession().setAttribute("rq", rq);

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
                req.getSession().setAttribute("rq", rq);

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
            req.getSession().setAttribute("rq", rq);

            return ResponseEntity.ok("자동 로그인 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }
    }

    @RequestMapping("/usr/member/google")
    public void googleCallback(@RequestParam("code") String code,
                               HttpServletRequest req,
                               HttpServletResponse resp) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. 토큰 요청
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", "구글 클라이언트 키");
            params.add("client_secret", "구글 시크릿 키");
            params.add("redirect_uri", "http://localhost:8080/usr/member/google");
            params.add("grant_type", "authorization_code");

            Map<String, Object> tokenResponse = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token", params, Map.class
            );

            String accessToken = (String) tokenResponse.get("access_token");

            // 2. 사용자 정보 요청
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
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String socialId = (String) userInfo.get("id"); // ✅ 고유 ID 따로 받아옴

            // 3. 회원 생성 or 조회
            Member member = memberService.getOrCreateSocialMember("google", socialId, email, name);

            // 4. 세션 저장
            req.getSession().setAttribute("loginedMemberId", member.getId());
            req.getSession().setAttribute("loginedMember", member);
            req.setAttribute("rq", new Rq(req, resp, memberService));

            // 5. Firebase 토큰 발급 (✅ uid: google_소셜ID)
            String uid = "google_" + socialId;
            String firebaseToken = memberService.createFirebaseCustomToken(uid);
            req.getSession().setAttribute("firebaseToken", firebaseToken);

            // ✅ 6. 부모 창에 메시지 전송 후 창 닫기
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<script>");
            out.println("window.opener.postMessage('socialLoginSuccess', '*');");
            out.println("window.close();");
            out.println("</script>");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                resp.setContentType("text/html; charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println("<script>");
                out.println("alert('구글 로그인 실패');");
                out.println("window.close();");
                out.println("</script>");
            } catch (Exception ignored) {
            }
        }
    }

    // 네이버 로그인 콜백 처리
    @RequestMapping("/usr/member/naver")
    @ResponseBody
    public String naverCallback(@RequestParam("code") String code,
                                @RequestParam("state") String state,
                                HttpServletRequest req, HttpServletResponse resp) {

        try {

            RestTemplate restTemplate = new RestTemplate();

            // 네이버 애플리케이션 등록 정보
            String clientId = "ZdyW5GGtNSgCCaduup7_";          // 네이버 Client ID
            String clientSecret = "pJh4IlGi2_";  // 네이버 Client Secret
            String redirectUri = "http://localhost:8080/usr/member/naver";  // 콜백 URI

            // 1️⃣ access_token 요청 URL 구성
            String tokenUrl = "https://nid.naver.com/oauth2.0/token" +
                    "?grant_type=authorization_code" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&code=" + code +
                    "&state=" + state;


            // 2️⃣ 토큰 요청 (GET 방식)
            ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(tokenUrl, Map.class);

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 3️⃣ 사용자 정보 요청을 위한 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            // 4️⃣ 네이버 사용자 정보 요청
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // 5️⃣ 응답 파싱
            Map<String, Object> body = userInfoResponse.getBody();
            Map<String, Object> response = (Map<String, Object>) body.get("response");

            // 6️⃣ 사용자 정보 추출
            String socialId = String.valueOf(response.get("id"));  // 네이버 고유 ID
            String name = (String) response.get("name");           // 이름
            String email = (String) response.get("email");         // 이메일


            // 7️⃣ 회원 DB에 등록 또는 기존 회원 로그인 처리
            Member member = memberService.getOrCreateSocialMember("naver", socialId, email, name);

            // 8️⃣ 세션 등록 (RQ 객체를 이용한 로그인 처리)
            Rq rq = new Rq(req, resp, memberService);
            rq.login(member);
            req.getSession().setAttribute("rq", rq);

            // 🔥 Firebase 토큰 추가
            String uid = member.getSocialProvider() + "_" + member.getSocialId();
            String firebaseToken = memberService.createFirebaseCustomToken(uid);
            req.getSession().setAttribute("firebaseToken", firebaseToken);

            // ✅ 팝업 방식 Firebase 연동용 JS 리턴
            return """
                    <script>
                        window.opener.postMessage("socialLoginSuccess", "*");
                        window.close();
                    </script>
                    """;

        } catch (Exception e) {
            // ⚠ 예외 처리 (토큰 요청 실패, 사용자 정보 오류 등)
            e.printStackTrace();
            System.out.println("❌ [ERROR] naverCallback 예외 발생: " + e.getMessage());
            return "redirect:/usr/member/login?error=naver";
        }
    }

    @RequestMapping("/usr/member/firebase-token")
    @ResponseBody
    public ResultData<Map<String, String>> generateFirebaseToken(HttpServletRequest req, HttpServletResponse resp) {
        Integer memberId = (Integer) req.getSession().getAttribute("loginedMemberId");

        System.out.println("📥 [로그] firebase-token 요청 도착");
        System.out.println("   - 로그인된 memberId: " + memberId);

        if (memberId == null) return ResultData.from("F-1", "로그인 후 이용 가능합니다.");

        Member loginedMember = memberService.getMemberById(memberId);
        if (loginedMember == null) return ResultData.from("F-2", "회원 정보를 찾을 수 없습니다.");

        try {
            String uid = UUID.randomUUID().toString();
            String email = loginedMember.getEmail();
            String name = loginedMember.getNickname();
            String provider = loginedMember.getSocialProvider();
            if (provider == null || provider.trim().isEmpty()) provider = "email";

            System.out.println("   - 이메일: " + email);

            try {
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setUid(uid)
                        .setEmail(email)
                        .setDisplayName(name)
                        .setEmailVerified(true);

                FirebaseAuth.getInstance().createUser(request);
                System.out.println("✅ [로그] Firebase 새 사용자 등록 완료");

            } catch (FirebaseAuthException e) {
                if (e.getErrorCode().equals("EMAIL_ALREADY_EXISTS") || e.getMessage().contains("EMAIL_EXISTS")) {
                    System.out.println("⚠️ [로그] 이미 등록된 사용자 → uid 조회");
                    try {
                        UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(email);
                        uid = existingUser.getUid();
                        System.out.println("   - 기존 UID: " + uid);
                    } catch (FirebaseAuthException ex) {
                        System.out.println("❌ [로그] UID 조회 실패: " + ex.getMessage());
                        return ResultData.from("F-5", "기존 UID 조회 실패: " + ex.getMessage());
                    }
                } else {
                    System.out.println("❌ [로그] Firebase 사용자 등록 실패: " + e.getMessage());
                    return ResultData.from("F-4", "Firebase 사용자 등록 실패: " + e.getMessage());
                }
            }

            String customToken = FirebaseAuth.getInstance().createCustomToken(uid);
            System.out.println("✅ [로그] 커스텀 토큰 발급 완료");

            Map<String, String> data = new HashMap<>();
            data.put("token", customToken);
            data.put("provider", provider);

            return ResultData.from("S-1", "토큰 생성 성공", data);

        } catch (Exception e) {
            return ResultData.from("F-3", "토큰 생성 실패: " + e.getMessage());
        }
    }


    @RequestMapping("/usr/member/firebase-session-login")
    @ResponseBody
    public ResultData doFirebaseSessionLogin(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String idToken = body.get("idToken");

        System.out.println("📥 [로그] firebase-session-login 요청 도착");
        System.out.println("📥 [로그] 전달된 idToken: " + (idToken != null ? "존재함" : "없음"));

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            String uid = decodedToken.getUid();
            String name = decodedToken.getName(); // ✅ 이름 정보 추가

            System.out.println("✅ [로그] Firebase 인증 성공");
            System.out.println("   - UID: " + uid);
            System.out.println("   - 이메일: " + email);

            if (Ut.isEmpty(email)) {
                return ResultData.from("F-2", "이메일 정보 없음");
            }

            // ✅ 기존 회원 조회
            Member member = memberService.findByEmail(email);

            // ❗ 없으면 자동 가입
            if (member == null) {
                System.out.println("📌 [로그] 회원 정보 없음 → 자동 가입 시도");

                String provider = "email";
                if (uid != null && uid.contains("_")) {
                    provider = uid.split("_")[0];
                }

                member = memberService.getOrCreateByEmail(email, name, provider);

                if (member == null) {
                    System.out.println("❌ [로그] 자동 가입 실패");
                    return ResultData.from("F-9", "회원 자동 가입 실패");
                }

                System.out.println("📌 [로그] 자동 가입 완료 → ID: " + member.getId());
            }

            // ✅ 세션 저장
            req.getSession().setAttribute("loginedMemberId", member.getId());
            req.getSession().setAttribute("loginedMember", member);

            System.out.println("✅ [로그] 세션에 로그인 정보 저장 완료");
            System.out.println("   - memberId: " + member.getId());
            System.out.println("   - nickname: " + member.getNickname());

            return ResultData.from("S-1", "세션 로그인 완료");

        } catch (FirebaseAuthException e) {
            System.out.println("❌ [로그] Firebase 인증 실패: " + e.getMessage());
            return ResultData.from("F-1", "Firebase 인증 실패: " + e.getMessage());
        }
    }

}