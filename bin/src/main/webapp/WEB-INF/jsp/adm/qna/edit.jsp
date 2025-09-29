<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>

<section class="p-6">
    <h1 class="text-xl font-bold mb-4">QnA 수정</h1>

    <form method="post" action="/adm/qna/doModify">
        <input type="hidden" name="id" value="${qna.id}">
        <div class="mb-4">
            <label class="block mb-1 font-semibold">제목</label>
            <input type="text" name="title" class="w-full border p-2" value="${qna.title}">
        </div>
        <div class="mb-4">
            <label class="block mb-1 font-semibold">내용</label>
            <textarea name="body" rows="5" class="w-full border p-2">${qna.body}</textarea>
        </div>
        <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded">수정 완료</button>
    </form>

    <a href="/adm/qna/list" class="inline-block mt-6 text-blue-600 underline">← 목록으로 돌아가기</a>
</section>
