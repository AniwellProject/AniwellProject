<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>

<div class="p-6 max-w-2xl mx-auto bg-white shadow rounded">
    <h1 class="text-xl font-bold mb-4">자주 묻는 질문 등록</h1>

    <form action="/adm/qna/doWrite" method="post" class="space-y-4">
        <input type="text" name="title" placeholder="제목" class="w-full border px-3 py-2 rounded" required />
        <textarea name="body" rows="6" placeholder="내용을 입력하세요" class="w-full border px-3 py-2 rounded" required></textarea>
        <button class="bg-blue-600 text-white px-4 py-2 rounded" type="submit">등록</button>
    </form>

    <div class="mt-4">
        <a href="/adm/qna/list" class="text-blue-600 underline">← 목록으로</a>
    </div>
</div>
