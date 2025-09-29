<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>
<div class="w-full h-full bg-gradient-to-l from-[#f6fce7] to-[#c8dfb4] min-h-screen p-8">
  <!-- 관리자 페이지 제목 -->
  <h1 class="text-xl font-bold text-[#2c6e49] mb-4">관리자 페이지</h1>

  <!-- 전체 wrapper -->
  <div class="flex gap-6">

    <!-- 좌측 사이드바 -->
    <div class="flex flex-col gap-4 w-32">
      <img src="/img/aniwell-logo.png" alt="Aniwell" class="w-24 mb-4" />

      <button onclick="location.href='/usr/member/myPage'" class="bg-[#d7e7a9] text-black px-3 py-2 rounded hover:bg-[#c5dc7b]">나의 정보</button>
      <button onclick="location.href='/usr/walkCrew/list'" class="bg-[#d7e7a9] text-black px-3 py-2 rounded hover:bg-[#c5dc7b]">산책 크루</button>

      <a href="/adm/home/main" class="mt-8 bg-yellow-400 text-white px-3 py-2 rounded text-center font-bold">관리자페이지</a>
    </div>

    <!-- 본문 영역 -->
    <div class="flex-1">

      <!-- 반려동물 등록증 카드 영역 -->
      <div class="relative flex gap-4 justify-start items-start h-[220px] mb-10">
        <!-- 회색 카드 -->
        <div class="bg-gray-300 text-black p-4 rounded-xl w-[240px] h-[180px] shadow-md z-0 cursor-pointer" onclick="location.href='/usr/pet/list?memberId=${rq.loginedMember.id}'">
          <div class="font-bold text-sm mb-2">🐾 반려동물등록증</div>
          <p>이름: 백머히</p>
          <p>평종: 삼사리</p>
          <p>생일: -</p>
          <p>성별: 암콤</p>
          <p class="text-xs mt-1">특: 길이었는데 놀래든이 이마 주름이쇤</p>
        </div>

        <!-- 강조된 가운데 카드 -->
        <div class="absolute left-20 top-[-20px] bg-white text-black p-4 rounded-xl w-[240px] h-[200px] shadow-lg z-10 border border-gray-300 cursor-pointer" onclick="location.href='/usr/pet/list?memberId=${rq.loginedMember.id}'">
          <div class="font-bold text-sm mb-2">🐾 반려동물등록증</div>
          <p>이름: 백머히</p>
          <p>평종: 삼사리</p>
          <p>생일: -</p>
          <p>성별: 암콤</p>
          <p class="text-xs mt-1">특: 길이었는데 놀래든이 이마 주름이쇤</p>
          <p class="text-right text-xs mt-2">2025. 07. 05</p>
        </div>

        <!-- 오른쪽 카드 -->
        <div class="bg-gray-300 text-black p-4 rounded-xl w-[240px] h-[180px] shadow-md z-0 cursor-pointer" onclick="location.href='/usr/pet/list?memberId=${rq.loginedMember.id}'">
          <div class="font-bold text-sm mb-2">🐾 반려동물등록증</div>
          <img src="/img/sample-cat.jpg" alt="고양이" class="w-full h-24 object-cover rounded mb-1" />
          <p class="text-right text-xs">2025. 07. 05</p>
        </div>
      </div>

      <!-- 참가 목록 + 이미지 -->
      <div class="flex gap-6">
        <!-- 참가 목록 -->
        <div class="bg-[#fffde8] rounded-xl p-6 w-1/2 shadow">
          <h2 class="font-bold text-gray-700 mb-4">참가 목록</h2>

          <div class="bg-[#fff4b8] p-3 rounded mb-2 cursor-pointer" onclick="location.href='/usr/walkCrew/list'">
            <p class="font-bold">크루명</p>
            <p class="text-sm">크루 한줄소개</p>
          </div>
          <div class="bg-[#fff4b8] p-3 rounded mb-2 cursor-pointer" onclick="location.href='/usr/walkCrew/list'">
            <p class="font-bold">크루명</p>
            <p class="text-sm">크루 한줄소개</p>
          </div>
          <div class="bg-[#fff4b8] p-3 rounded cursor-pointer" onclick="location.href='/usr/walkCrew/list'">
            <p class="font-bold">크루명</p>
            <p class="text-sm">크루 한줄소개</p>
          </div>
        </div>

        <!-- 이미지 -->
        <div class="bg-[#fffde8] rounded-xl p-6 w-1/2 shadow flex items-center justify-center">
          <img src="/img/walk-illustration.png" alt="산책 이미지" class="w-full max-w-sm rounded" />
        </div>
      </div>
    </div>
  </div>
</div>