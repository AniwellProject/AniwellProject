<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="CHECKPW" />
<%@ include file="../common/head.jspf" %>

<section class="mt-24 text-lg px-4">
  <div class="mx-auto max-w-xl bg-white p-6 rounded-xl shadow-md">
    <h1 class="text-2xl font-bold mb-6 text-center">비밀번호 확인</h1>

    <form action="../member/doCheckPw" method="POST">
      <table class="w-full table-auto text-sm">
        <tbody>
          <tr class="border-t">
            <th class="text-left px-4 py-2 w-1/3">아이디</th>
            <td class="px-4 py-2">${rq.loginedMember.loginId}</td>
          </tr>
          <tr class="border-t">
            <th class="text-left px-4 py-2">비밀번호</th>
            <td class="px-4 py-2">
              <input name="loginPw" type="password" placeholder="비밀번호 입력" class="input input-sm w-full" autocomplete="off" />
            </td>
          </tr>
          <tr class="border-t">
            <td colspan="2" class="text-center py-4">
              <button type="submit" class="btn btn-primary">확인</button>
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
