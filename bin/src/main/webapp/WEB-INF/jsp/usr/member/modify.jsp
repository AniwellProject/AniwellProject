<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="MEMBER MODIFY" />
<%@ include file="../common/head.jspf" %>

<script type="text/javascript">
  function MemberModify__submit(form) {
    form.loginPw.value = form.loginPw.value.trim();

    if (form.loginPw.value.length > 0) {
      form.loginPwConfirm.value = form.loginPwConfirm.value.trim();
      if (form.loginPwConfirm.value.length == 0) {
        alert('비밀번호 확인을 입력하세요.');
        return;
      }

      if (form.loginPwConfirm.value != form.loginPw.value) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
      }
    }

    form.submit();
  }
</script>

<section class="mt-24 text-lg px-4">
  <div class="mx-auto max-w-2xl bg-white p-6 rounded-xl shadow-md">
    <h1 class="text-2xl font-bold mb-6 text-center">회원정보 수정</h1>

    <form action="../member/doModify" method="POST" onsubmit="MemberModify__submit(this); return false;">
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
          <tr class="border-t">
            <th class="text-left px-4 py-2">새 비밀번호</th>
            <td class="px-4 py-2">
              <input name="loginPw" type="password" placeholder="새 비밀번호" class="input input-sm w-full" autocomplete="off">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">비밀번호 확인</th>
            <td class="px-4 py-2">
              <input name="loginPwConfirm" type="password" placeholder="비밀번호 확인" class="input input-sm w-full" autocomplete="off">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">이름</th>
            <td class="px-4 py-2">
              <input name="name" type="text" value="${rq.loginedMember.name}" class="input input-sm w-full" autocomplete="off">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">닉네임</th>
            <td class="px-4 py-2">
              <input name="nickname" type="text" value="${rq.loginedMember.nickname}" class="input input-sm w-full" autocomplete="off">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">이메일</th>
            <td class="px-4 py-2">
              <input name="email" type="email" value="${rq.loginedMember.email}" class="input input-sm w-full" autocomplete="off">
            </td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">전화번호</th>
            <td class="px-4 py-2">
              <input name="cellphone" type="text" value="${rq.loginedMember.cellphone}" class="input input-sm w-full" autocomplete="off">
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
