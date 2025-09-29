<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="MEMBER MODIFY" />
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

  .file-label {
    margin-top: 8px;
    font-size: 0.9rem;
    color: #555;
    cursor: pointer;
  }

  #photoInput {
    display: none;
  }

  .pw-section {
    display: none;
  }

  .pw-msg {
    font-size: 0.8rem;
    margin-top: 4px;
  }
</style>

<script>
  function MemberModify__submit(form) {
    const pwToggle = document.getElementById('pwChangeToggle');
    const isPwChange = pwToggle.dataset.active === 'true';

    if (!isPwChange) {
      form.loginPw.value = '';
      form.loginPwConfirm.value = '';
    } else {
      const pw = form.loginPw.value.trim();
      const confirm = form.loginPwConfirm.value.trim();

      if (pw.length < 4) {
        alert("비밀번호는 4자 이상 입력해야 합니다.");
        return;
      }

      if (pw !== confirm) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }
    }

    form.submit();
  }

  function togglePwFields() {
  const btn = document.getElementById('pwChangeToggle');
  const isActive = btn.dataset.active === 'true';
  const section = document.querySelectorAll('.pw-section');
  const pwInput = document.querySelector('[name="loginPw"]');
  const pwConfirm = document.querySelector('[name="loginPwConfirm"]');

  btn.dataset.active = !isActive;
  btn.textContent = isActive ? '비밀번호 변경' : '비밀번호 변경 취소';

  section.forEach(row => {
    row.style.display = isActive ? 'none' : 'table-row';
  });

  if (isActive) {
    // 변경 취소: disabled 처리 → 서버에 안 넘어감
    pwInput.disabled = true;
    pwConfirm.disabled = true;
    pwInput.value = '';
    pwConfirm.value = '';
  } else {
    // 변경 시작: 입력 가능
    pwInput.disabled = false;
    pwConfirm.disabled = false;
  }

  document.getElementById('pwCheckMsg').textContent = '';
}


  function checkPwMatch() {
    const pw = document.querySelector('[name="loginPw"]').value.trim();
    const confirm = document.querySelector('[name="loginPwConfirm"]').value.trim();
    const msg = document.getElementById('pwCheckMsg');

    if (pw.length === 0 || confirm.length === 0) {
      msg.textContent = '';
      return;
    }

    if (pw === confirm) {
      msg.textContent = '✅ 비밀번호가 일치합니다.';
      msg.style.color = 'green';
    } else {
      msg.textContent = '❌ 비밀번호가 일치하지 않습니다.';
      msg.style.color = 'red';
    }
  }

  function previewProfilePhoto(input) {
    const file = input.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function (e) {
      document.getElementById('profilePhoto').src = e.target.result;
    };
    reader.readAsDataURL(file);
  }
</script>

<section class="mt-24 text-lg px-4">
  <div class="mx-auto max-w-2xl bg-white p-6 rounded-xl shadow-md">
    <h1 class="text-2xl font-bold mb-6 text-center">회원정보 수정</h1>

    <!-- 회원정보 수정 폼 -->
    <form action="/usr/member/doModify" method="POST" onsubmit="MemberModify__submit(this); return false;" enctype="multipart/form-data">
      <!-- 프로필 사진 영역 -->
    <div class="flex flex-col items-center mb-6">
      <c:choose>
        <c:when test="${not empty rq.loginedMember.photo}">
          <img id="profilePhoto" class="photo" src="${rq.loginedMember.photo}" alt="프로필 사진" />
        </c:when>
        <c:otherwise>
          <img id="profilePhoto" class="photo" src="/img/default-card.png" alt="기본 프로필 사진" />
        </c:otherwise>
      </c:choose>


      <label class="file-label" for="photoInput">📷 사진 변경하기</label>
      <input name="photoFile" type="file" id="photoInput" accept="image/*" onchange="previewProfilePhoto(this)" />
    </div>

      <table class="w-full table-auto text-sm">
        <tbody>
          <tr class="border-t">
            <th class="text-left px-4 py-2 w-1/3">가입일</th>
            <td class="px-4 py-2">${rq.loginedMember.regDate}</td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">아이디</th>
            <td class="px-4 py-2">${rq.loginedMember.loginId}</td>
          </tr>

          <!-- 비밀번호 변경 토글 버튼 -->
          <tr class="border-t">
            <th class="text-left px-4 py-2">비밀번호</th>
            <td class="px-4 py-2">
              <button type="button" id="pwChangeToggle" class="btn btn-outline" data-active="false" onclick="togglePwFields()">비밀번호 변경</button>
            </td>
          </tr>

          <!-- 새 비밀번호 입력 -->
          <tr class="pw-section border-t">
            <th class="text-left px-4 py-2">새 비밀번호</th>
            <td class="px-4 py-2">
              <input name="loginPw" type="password" placeholder="새 비밀번호" class="input input-sm w-full"
       autocomplete="new-password" oninput="checkPwMatch()" disabled>

            </td>
          </tr>
          <!-- 비밀번호 확인 -->
          <tr class="pw-section border-t">
            <th class="text-left px-4 py-2">비밀번호 확인</th>
            <td class="px-4 py-2">
              <input name="loginPwConfirm" type="password" placeholder="비밀번호 다시 입력" class="input input-sm w-full"
       autocomplete="new-password" oninput="checkPwMatch()" disabled>

              <div id="pwCheckMsg" class="pw-msg"></div>
            </td>
          </tr>

          <!-- 기본 회원 정보 -->
          <tr class="border-t">
            <th class="text-left px-4 py-2">이름</th>
            <td class="px-4 py-2">
              <input name="name" type="text" value="${rq.loginedMember.name}" class="input input-sm w-full">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">닉네임</th>
            <td class="px-4 py-2">
              <input name="nickname" type="text" value="${rq.loginedMember.nickname}" class="input input-sm w-full">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">이메일</th>
            <td class="px-4 py-2">
              <input name="email" type="email" value="${rq.loginedMember.email}" class="input input-sm w-full">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">전화번호</th>
            <td class="px-4 py-2">
              <input name="cellphone" type="text" value="${rq.loginedMember.cellphone}" class="input input-sm w-full">
            </td>
          </tr>

          <tr class="border-t">
            <td colspan="2" class="text-center py-4">
              <button class="btn btn-primary">수정하기</button>
            </td>
          </tr>
        </tbody>
      </table>
    </form>

    <div class="text-center mt-4">
      <button class="btn" type="button" onclick="history.back()">뒤로가기</button>
    </div>
  </div>
</section>

<%@ include file="../common/foot.jspf" %>
