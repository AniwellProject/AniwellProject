<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>

<section class="p-6">
    <h1 class="text-xl font-bold mb-4">QnA 상세</h1>

    <div class="bg-white border rounded p-4 mb-6">
        <h2 class="text-lg font-semibold mb-2">${qna.title}</h2>
        <p class="text-sm text-gray-600 mb-2">작성자: ${qna.memberName}</p>
        <div class="border-t pt-3 mb-3">
            <p>${qna.body}</p>
            <p class="text-sm text-gray-500 mb-1">
                비밀글 여부:
                <c:choose>
                    <c:when test="${qna.secret}">✅ 비밀글</c:when>
                    <c:otherwise>❌ 공개글</c:otherwise>
                </c:choose>
            </p>
        </div>

        <!-- 질문 수정/삭제 버튼 -->
        <div class="space-x-2">
            <a href="/adm/qna/edit?id=${qna.id}" class="text-yellow-600 underline">질문 수정</a>
            <form action="/adm/qna/doDelete" method="post" style="display:inline;">
                <input type="hidden" name="id" value="${qna.id}"/>
                <button type="submit" class="text-red-600 underline bg-transparent border-none"
                        onclick="return confirm('질문을 삭제할까요?')">질문 삭제
                </button>
            </form>
        </div>
    </div>

    <c:if test="${qna.isFaq != null && qna.isFaq != 1}">
    <p>isFaq 값: ${qna.isFaq}</p>
        <c:choose>
            <c:when test="${empty answer}">
                <form action="/adm/qna/doAnswer" method="post" class="space-y-4">
                    <input type="hidden" name="qnaId" value="${qna.id}"/>
                    <input type="hidden" name="vetName" value="관리자수의사"/>
                    <textarea name="answer" rows="5" class="w-full border p-2" placeholder="답변 입력"></textarea>
                    <button class="bg-blue-600 text-white px-4 py-2 rounded" type="submit">답변 등록</button>
                </form>
            </c:when>
            <c:otherwise>
                <div class="border p-4 bg-gray-100 mb-4">
                    <div class="text-sm text-gray-500 mb-1">답변자: ${answer.vetName} / ${answer.answerAt}</div>
                    <p>${answer.answer}</p>
                </div>
                <form action="/adm/qna/doUpdateAnswer" method="post" class="space-y-4">
                    <input type="hidden" name="id" value="${answer.id}"/>
                    <textarea name="answer" rows="5" class="w-full border p-2">${answer.answer}</textarea>
                    <button class="bg-yellow-500 text-white px-4 py-2 rounded" type="submit">답변 수정</button>
                </form>
                <form action="/adm/qna/doDeleteAnswer" method="post" class="mt-2">
                    <input type="hidden" name="id" value="${answer.id}"/>
                    <button class="bg-red-500 text-white px-4 py-2 rounded" type="submit">답변 삭제</button>
                </form>
            </c:otherwise>
        </c:choose>
    </c:if>
    <a href="/adm/qna/list" class="inline-block mt-6 text-blue-600 underline">← 목록으로 돌아가기</a>
</section>
