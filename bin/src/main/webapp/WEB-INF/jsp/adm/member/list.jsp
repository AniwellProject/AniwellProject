<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원 목록</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.2.19/tailwind.min.css"/>
</head>
<body class="p-8 bg-gray-50 min-h-screen">

<h1 class="text-2xl font-bold mb-6">회원 관리</h1>

<form action="/adm/member/list" method="get" class="mb-4 flex gap-2">
    <select name="searchType" class="border p-2 rounded">
        <option value="authLevel" <c:if test="${param.searchType == 'authLevel'}">selected</c:if>>사용자 권한</option>
        <option value="loginId" <c:if test="${param.searchType == 'loginId'}">selected</c:if>>아이디</option>
        <option value="name" <c:if test="${param.searchType == 'name'}">selected</c:if>>이름</option>
        <option value="id" <c:if test="${param.searchType == 'id'}">selected</c:if>>번호</option>
    </select>

    <input type="text" name="searchKeyword"
           value="${fn:escapeXml(param.searchKeyword)}"
           placeholder="이름 또는 이메일을 입력하세요"
           class="border p-2 rounded flex-grow"/>

    <button class="bg-yellow-400 px-4 py-2 rounded text-white">검색</button>
</form>


<table class="table-auto w-full border-collapse border border-gray-300">
    <thead class="bg-gray-200 text-gray-700">
    <tr>
        <th class="border px-4 py-2">번호</th>
        <th class="border px-4 py-2">이름</th>
        <th class="border px-4 py-2">이메일</th>
        <th class="border px-4 py-2">아이디</th>
        <th class="border px-4 py-2">권한 등급</th>
        <th class="border px-4 py-2">증명서</th>
        <th class="border px-4 py-2">인증 상태</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="member" items="${members}">
        <tr class="text-center bg-white">
            <td class="border px-2 py-1">${member.id}</td>
            <td class="border px-2 py-1">${member.name}</td>
            <td class="border px-2 py-1">${member.email}</td>
            <td class="border px-2 py-1">${member.loginId}</td>
            <td class="border px-2 py-1">
                <form action="/adm/member/changeAuth" method="post">
                    <input type="hidden" name="id" value="${member.id}"/>
                    <select name="authLevel" class="border px-2 py-1 text-sm">
                        <option value="1" ${member.authLevel == 1 ? 'selected' : ''}>일반</option>
                        <option value="3" ${member.authLevel == 3 ? 'selected' : ''}>수의사</option>
                        <option value="7" ${member.authLevel == 7 ? 'selected' : ''}>관리자</option>
                    </select>
                    <button class="text-sm text-blue-500 ml-2">변경</button>
                </form>
            </td>
            <td class="border px-2 py-1">
                <c:choose>
                    <c:when test="${not empty member.vetCertUrl}">
                        <a href="/gen/file/download?path=vet_certificates/${member.vetCertUrl}" target="_blank"
                           class="text-blue-600 underline">보기</a>
                    </c:when>
                    <c:otherwise>
                        없음
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="border px-2 py-1">
                <c:choose>
                    <c:when test="${member.vetCertApproved == 1}">✅ 인증</c:when>
                    <c:when test="${member.vetCertApproved == 2}">❌ 거절</c:when>
                    <c:otherwise>⏳ 대기 중</c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</body>
</html>
