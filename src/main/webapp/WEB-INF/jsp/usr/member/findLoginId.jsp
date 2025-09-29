<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="아이디 찾기" />
<%@ include file="../common/head.jspf" %>

<script>
	function MemberFindLoginId__submit(form) {
		form.name.value = form.name.value.trim();
		form.email.value = form.email.value.trim();

		if (form.name.value.length == 0) {
			alert('이름을 입력해주세요');
			form.name.focus();
			return false;
		}
		if (form.email.value.length == 0) {
			alert('이메일을 입력해주세요');
			form.email.focus();
			return false;
		}
		return true;
	}
</script>

<section class="mt-24 text-lg px-4">
	<div class="mx-auto max-w-xl bg-white p-6 rounded-xl shadow-md">
		<h1 class="text-2xl font-bold mb-6 text-center">아이디 찾기</h1>
		<form action="../member/doFindLoginId" method="POST" onsubmit="return MemberFindLoginId__submit(this);">
			<input type="hidden" name="afterFindLoginIdUri" value="${param.afterFindLoginIdUri}" />
			<div class="mb-4">
				<label class="block mb-1">이름</label>
				<input type="text" name="name" class="input input-bordered w-full" placeholder="이름을 입력해주세요" autocomplete="off">
			</div>
			<div class="mb-4">
				<label class="block mb-1">이메일</label>
				<input type="text" name="email" class="input input-bordered w-full" placeholder="이메일을 입력해주세요" autocomplete="off">
			</div>
			<div class="flex justify-between mt-6">
				<button type="submit" class="btn btn-primary">아이디 찾기</button>
				<a class="btn btn-outline" href="../member/login">로그인</a>
				<a class="btn btn-outline btn-success" href="${rq.findLoginPwUri}">비밀번호 찾기</a>
			</div>
		</form>
		<div class="text-center mt-4">
			<button class="btn btn-outline" type="button" onclick="history.back();">뒤로가기</button>
		</div>
	</div>
</section>

<%@ include file="../common/foot.jspf" %>
