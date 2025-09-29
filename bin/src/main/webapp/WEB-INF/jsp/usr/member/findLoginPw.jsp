<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="비밀번호 찾기" />
<%@ include file="../common/head.jspf" %>

<script>
	let MemberFindLoginPw__submitFormDone = false;

	function MemberFindLoginPw__submit(form) {
		if (MemberFindLoginPw__submitFormDone) return false;

		form.loginId.value = form.loginId.value.trim();
		form.email.value = form.email.value.trim();

		if (form.loginId.value.length == 0) {
			alert('아이디를 입력해주세요');
			form.loginId.focus();
			return false;
		}
		if (form.email.value.length == 0) {
			alert('이메일을 입력해주세요');
			form.email.focus();
			return false;
		}

		alert('메일로 임시 비밀번호를 발송했습니다');
		MemberFindLoginPw__submitFormDone = true;
		return true;
	}
</script>

<section class="mt-24 text-lg px-4">
	<div class="mx-auto max-w-xl bg-white p-6 rounded-xl shadow-md">
		<h1 class="text-2xl font-bold mb-6 text-center">비밀번호 찾기</h1>
		<form action="../member/doFindLoginPw" method="POST" onsubmit="return MemberFindLoginPw__submit(this);">
			<input type="hidden" name="afterFindLoginPwUri" value="${param.afterFindLoginPwUri}" />
			<div class="mb-4">
				<label class="block mb-1">아이디</label>
				<input type="text" name="loginId" class="input input-bordered w-full" placeholder="아이디를 입력해주세요" autocomplete="off">
			</div>
			<div class="mb-4">
				<label class="block mb-1">이메일</label>
				<input type="text" name="email" class="input input-bordered w-full" placeholder="이메일을 입력해주세요" autocomplete="off">
			</div>
			<div class="flex justify-between mt-6">
				<button type="submit" class="btn btn-primary">비밀번호 찾기</button>
				<a class="btn btn-outline" href="../member/login">로그인</a>
			</div>
		</form>
		<div class="text-center mt-4">
			<button class="btn btn-outline" type="button" onclick="history.back();">뒤로가기</button>
		</div>
	</div>
</section>

<%@ include file="../common/foot.jspf" %>
