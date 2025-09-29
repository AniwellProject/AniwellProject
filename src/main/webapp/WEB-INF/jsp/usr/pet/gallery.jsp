<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
  <title>감정 결과 분석</title>
  <style>
    body {
      font-family: 'SUIT', sans-serif;
      background-color: #f9f9f9;
      margin: 0;
      padding: 30px;
    }

    h2 {
      text-align: center;
      color: #555;
      margin-bottom: 30px;
    }

    #emotionButtons {
      text-align: center;
      margin-bottom: 30px;
    }

    .emotion-btn {
      padding: 10px 18px;
      margin: 6px;
      border: none;
      background-color: #f0f0f0;
      border-radius: 20px;
      font-size: 14px;
      font-weight: bold;
      cursor: pointer;
      transition: background-color 0.3s;
      box-shadow: 1px 1px 4px rgba(0,0,0,0.1);
    }

    .emotion-btn:hover {
      background-color: #ffe28a;
    }

    .emotion-btn.active {
      background-color: #ffda5a;
      color: white;
    }

    #results {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 20px;
    }

    .result-item {
      width: 220px;
      background-color: #ffffff;
      border-radius: 16px;
      padding: 15px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.08);
      text-align: center;
      transition: transform 0.2s;
    }

    .result-item:hover {
      transform: translateY(-4px);
    }

    .result-item img {
      width: 100%;
      height: 160px;
      object-fit: cover;
      border-radius: 12px;
      margin-bottom: 10px;
    }

    .result-item span.label {
      display: block;
      margin-top: 4px;
      font-size: 14px;
      color: #777;
    }

    .hidden {
      display: none;
    }
  </style>
</head>
<body>

<h2>🐾 감정 결과 갤러리</h2>

<!-- 감정 필터 버튼 -->
<div id="emotionButtons">
  <button class="emotion-btn" onclick="filterByEmotion('happy')">😊 Happy</button>
  <button class="emotion-btn" onclick="filterByEmotion('relaxed')">😌 Relaxed</button>
  <button class="emotion-btn" onclick="filterByEmotion('sad')">😿 Sad</button>
  <button class="emotion-btn" onclick="filterByEmotion('scared')">😨 Scared</button>
</div>

<!-- 감정 결과 카드 -->
<div id="results">
  <c:forEach var="item" items="${analysisList}">
    <div class="result-item" data-emotion="${item.emotionResult}">
      <img src="${item.imagePath}" alt="Pet Image">
      <strong>${item.emotionResult}</strong>
      <span class="label">신뢰도: ${item.confidence * 100}%</span>
      <span class="label">날짜: ${item.analyzedAt}</span>
    </div>
  </c:forEach>
</div>

<script>
  function filterByEmotion(emotion) {
    const buttons = document.querySelectorAll('.emotion-btn');
    buttons.forEach(btn => btn.classList.remove('active'));

    const selectedBtn = [...buttons].find(btn => btn.textContent.toLowerCase().includes(emotion));
    if (selectedBtn) selectedBtn.classList.add('active');

    const items = document.querySelectorAll('.result-item');
    items.forEach(item => {
      if (item.dataset.emotion === emotion) {
        item.classList.remove('hidden');
      } else {
        item.classList.add('hidden');
      }
    });
  }
</script>

</body>
</html>
