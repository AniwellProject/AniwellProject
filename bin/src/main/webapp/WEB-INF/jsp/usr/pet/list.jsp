<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
<head>
    <title>반려동물 선택</title>
    <style>
        body {
            margin: 0;
            font-family: 'Arial';
            background: linear-gradient(to bottom right, #eef6dc, #d0e0b9);
            display: flex;
        }

        /* 사이드 메뉴 */
        .sidebar {
            width: 120px;
            background: linear-gradient(to bottom, #cfe6b8, #e3e9ce);
            display: flex;
            flex-direction: column;
            align-items: center;
            padding-top: 20px;
            box-shadow: 2px 0 10px rgba(0,0,0,0.1);
        }

        .logo {
            width: 100px;
            height: 100px;
            background: url('/img/logo.png') no-repeat center/contain;
            margin-bottom: 20px;
        }

        .menu-icon {
            width: 24px;
            height: 3px;
            background: #333;
            margin: 5px 0;
            border-radius: 2px;
        }

        .menu-button {
            margin: 20px 0;
            padding: 10px 14px;
            background: #b2d3a8;
            border: none;
            border-radius: 12px;
            font-weight: bold;
            cursor: pointer;
            box-shadow: 1px 1px 5px rgba(0,0,0,0.1);
        }

        /* 메인 컨텐츠 */
        .main {
            flex: 1;
            padding: 40px;
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        h2 {
            margin-bottom: 30px;
        }

        .carousel-container {
            position: relative;
            width: 900px;
            height: 280px;
            perspective: 1200px;
            margin: 0 0 50px 30%;
        }

        .card {
            position: absolute;
            width: 460px;
            height: 220px;
            padding: 20px;
            border-radius: 12px;
            background: white;
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
            transform-style: preserve-3d;
            transition: all 0.6s ease;
            opacity: 0.3;
        }

        .card.active {
            transform: translateX(0) rotateY(0) scale(1);
            z-index: 3;
            opacity: 1;
        }

        .card.left {
            transform: translateX(-240px) rotateY(30deg) scale(0.9);
            z-index: 2;
        }

        .card.right {
            transform: translateX(240px) rotateY(-30deg) scale(0.9);
            z-index: 2;
        }

        .card h3 {
            margin: 0;
            font-size: 16px;
            display: flex;
            align-items: center;
        }

        .card h3::before {
            content: "🐾";
            margin-right: 6px;
        }

        .card .content {
            margin-top: 10px;
            font-size: 14px;
        }

        .card img {
            float: right;
            width: 70px;
            height: 70px;
            border-radius: 8px;
            object-fit: cover;
            margin-left: 10px;
        }

        .card .date {
            margin-top: 20px;
            text-align: right;
            font-size: 12px;
            color: #555;
            border-top: 1px solid #ddd;
            padding-top: 6px;
        }

        /* 빈 카드 (디폴트) */
        .card.empty {
            background: url('/img/default-card.png') no-repeat center/cover;
            padding: 0;
        }

        .nav-buttons {
            margin-bottom: 40px;
        }

        .nav-buttons button {
            padding: 8px 16px;
            margin: 0 10px;
            background: #a8cbb5;
            border: none;
            border-radius: 10px;
            font-weight: bold;
            cursor: pointer;
        }

        /* 크루 목록 */
        .crew-section {
            display: flex;
            width: 800px;
            justify-content: space-between;
            background: #fffff7;
            border-radius: 20px;
            padding: 20px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }

        .crew-list {
            flex: 1;
        }

        .crew-item {
            background: #fef7cd;
            margin-bottom: 10px;
            padding: 10px;
            border-radius: 12px;
            font-size: 14px;
        }

        .crew-image {
            width: 180px;
            height: 180px;
            background: url('/img/walk-image.png') no-repeat center/cover;
            border-radius: 12px;
            margin-left: 20px;
        }
    </style>

    <script>
        let currentIndex = 0;
        let startX = 0;

        function updateCards() {
            const cards = document.querySelectorAll('.card');
            cards.forEach((card, i) => {
                card.classList.remove('left', 'right', 'active');
                if (i === currentIndex) {
                    card.classList.add('active');
                } else if (i === (currentIndex + 1) % cards.length) {
                    card.classList.add('right');
                } else if (i === (currentIndex - 1 + cards.length) % cards.length) {
                    card.classList.add('left');
                }
            });
        }

        function next() {
            const cards = document.querySelectorAll('.card');
            currentIndex = (currentIndex + 1) % cards.length;
            updateCards();
        }

        function prev() {
            const cards = document.querySelectorAll('.card');
            currentIndex = (currentIndex - 1 + cards.length) % cards.length;
            updateCards();
        }

        window.addEventListener('DOMContentLoaded', function () {
            updateCards();

            const container = document.querySelector('.carousel-container');

            // 모바일 터치 이벤트
            container.addEventListener('touchstart', function (e) {
                startX = e.touches[0].clientX;
            });

            container.addEventListener('touchend', function (e) {
                const endX = e.changedTouches[0].clientX;
                if (startX - endX > 50) {
                    next();
                } else if (endX - startX > 50) {
                    prev();
                }
            });

            // PC 마우스 드래그 이벤트
            container.addEventListener('mousedown', function (e) {
                startX = e.clientX;
            });

            container.addEventListener('mouseup', function (e) {
                const endX = e.clientX;
                if (startX - endX > 50) {
                    next();
                } else if (endX - startX > 50) {
                    prev();
                }
            });
        });
    </script>

</head>
<body>

<div class="sidebar">
    <div class="logo"></div>
    <div class="menu-icon"></div>
    <div class="menu-icon"></div>
    <div class="menu-icon"></div>
    <button class="menu-button">나의 정보</button>
    <button class="menu-button">산책 크루</button>
</div>

<div class="main">
    <h2>🐾 반려동물 등록증</h2>

    <div class="carousel-container">
        <c:forEach var="pet" items="${pets}" varStatus="status">
            <a href="/usr/pet/modify?petId=${pet.id}" class="card" style="text-decoration: none; color: inherit;">
                <h3>반려동물등록증</h3>
                <div class="content">
                    <c:choose>
                        <c:when test="${not empty pet.photo}">
                            <img src="${pet.photo}" alt="사진">
                        </c:when>
                        <c:otherwise>
                            <img src="/img/default-pet.png" alt="사진">
                        </c:otherwise>
                    </c:choose>
                    이름: ${pet.name} <br>
                    품종: ${pet.breed} <br>
                    생일: ${pet.birthDate} <br>
                    성별: ${pet.gender}
                </div>
                <div class="date">${pet.createdAt}</div>
            </a>
        </c:forEach>

        <c:if test="${fn:length(pets) < 3}">
            <c:forEach var="i" begin="1" end="${3 - fn:length(pets)}">
                <div class="card empty"></div>
            </c:forEach>
        </c:if>
    </div>

    <div style="margin-bottom: 40px;">
        <form action="/usr/pet/join" method="get">
            <button type="submit" style="padding: 10px 20px; background: #e3e9ce; border: none; border-radius: 10px; font-weight: bold; cursor: pointer;">
                + 반려동물 등록하기
            </button>
        </form>
    </div>

    <div class="crew-section">
        <div class="crew-list">
            <div class="crew-item">크루명<br>크루 한줄소개</div>
            <div class="crew-item">크루명</div>
            <div class="crew-item">크루명</div>
        </div>
        <div class="crew-image"></div>
    </div>
</div>

</body>
</html>
