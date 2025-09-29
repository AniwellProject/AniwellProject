<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="MYPAGE"/>
<%@ include file="../common/head.jspf" %>

<style>
  .photo {
    width: 120px;
    height: 120px;
    object-fit: cover;
    border-radius: 9999px;
    border: 3px solid #ccc;
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  }
</style>

<section class="mt-24 text-lg px-4">
  <div class="mx-auto max-w-2xl bg-white p-6 rounded-xl shadow-md">
    <h1 class="text-2xl font-bold mb-6 text-center">내 정보</h1>

    <!-- 📸 프로필 사진 -->
    <div class="flex justify-center mb-4">
      <c:choose>
        <c:when test="${not empty rq.loginedMember.photo}">
          <img class="photo" src="${rq.loginedMember.photo}" alt="프로필 사진" />
        </c:when>
        <c:otherwise>
          <img class="photo" src="/img/default-card.png" alt="기본 프로필 사진" />
        </c:otherwise>
      </c:choose>
    </div>

    <table class="w-full table-auto border text-sm">
      <tbody>
        <tr class="border-t">
          <th class="text-left px-4 py-2 w-1/3">아이디</th>
          <td class="px-4 py-2">${rq.loginedMember.loginId}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">이름</th>
          <td class="px-4 py-2">${rq.loginedMember.name}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">닉네임</th>
          <td class="px-4 py-2">${rq.loginedMember.nickname}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">이메일</th>
          <td class="px-4 py-2">${rq.loginedMember.email}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">전화번호</th>
          <td class="px-4 py-2">${rq.loginedMember.cellphone}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">가입일</th>
          <td class="px-4 py-2">${rq.loginedMember.regDate}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">수정일</th>
          <td class="px-4 py-2">${rq.loginedMember.updateDate}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">탈퇴여부</th>
          <td class="px-4 py-2">
            <c:choose>
              <c:when test="${rq.loginedMember.delStatus}">탈퇴</c:when>
              <c:otherwise>정상</c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">탈퇴일</th>
          <td class="px-4 py-2">${rq.loginedMember.delDate}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">권한 레벨</th>
          <td class="px-4 py-2">
            <c:choose>
              <c:when test="${rq.loginedMember.authLevel == 7}">관리자</c:when>
              <c:when test="${rq.loginedMember.authLevel == 3}">수의사</c:when>
              <c:otherwise>일반회원</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- ✅ 수의사 인증 업로드 폼 -->
    <form id="vetCertForm" action="doVetCertUpload" method="post" enctype="multipart/form-data" style="display: none;">
      <input type="file" id="vetCertFileInput" name="file" accept=".pdf,.jpg,.jpeg,.png" onchange="uploadVetCert()"/>
    </form>

    <!-- ✅ 수의사 신청자만 업로드 버튼 -->
    <c:if test="${rq.loginedMember.authName == '수의사' && rq.loginedMember.authLevel == 1}">
      <div class="text-center mt-4">
        <button type="button" onclick="document.getElementById('vetCertFileInput').click();"
                class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
          수의사 인증서 업로드
        </button>
      </div>
    </c:if>

    <!-- ✅ 수의사라면 인증서 상태 확인 버튼 -->
    <c:if test="${rq.loginedMember.authName == '수의사'}">
      <div class="text-center mt-4">
        <a href="myCert" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
          인증서 상태 확인
        </a>
      </div>
    </c:if>

    <!-- ✅ 수의사 인증 완료 메시지 -->
    <c:if test="${rq.loginedMember.authLevel == 3}">
      <div class="text-center mt-4 text-green-600 font-semibold">
        ✅ 수의사 인증이 완료되었습니다.
      </div>
    </c:if>

    <!-- ✅ 회원 정보 수정 / 탈퇴 / 뒤로가기 -->
    <div class="text-center mt-6">
      <a href="../member/checkPw" class="text-blue-600 underline hover:text-blue-800">회원정보 수정</a>
    </div>
    <div class="text-center mt-4">
      <button class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700" onclick="doWithdraw()">회원 탈퇴</button>
    </div>
    <div class="text-center mt-4">
      <button class="btn" type="button" onclick="history.back()">뒤로가기</button>
    </div>
  </div>
</section>

<script>
  function doWithdraw() {
    if (!confirm("정말 탈퇴하시겠습니까? 탈퇴 후에는 복구할 수 없습니다.")) return;

    fetch('/usr/member/doWithdraw', {
      method: 'POST'
    })
      .then(res => res.text())
      .then(scriptText => {
        const matched = scriptText.match(/<script>([\s\S]*?)<\/script>/);
        if (matched && matched[1]) {
          const actualScript = matched[1];
          const scriptEl = document.createElement('script');
          scriptEl.textContent = actualScript;
          document.body.appendChild(scriptEl);
        } else {
          console.error("스크립트 태그가 응답에 포함되지 않았습니다.");
        }
      });
  }

  function uploadVetCert() {
    const form = document.getElementById('vetCertForm');
    const fileInput = document.getElementById('vetCertFileInput');

    if (!fileInput.files || fileInput.files.length === 0) {
      alert("파일을 선택해주세요.");
      return;
    }

    form.submit();
  }
</script>

<%@ include file="../common/foot.jspf" %>
