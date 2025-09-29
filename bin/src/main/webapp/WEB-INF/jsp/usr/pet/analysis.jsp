<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
  <title>감정 결과 분석</title>
  <style>
    .emotion-btn {
      padding: 10px 20px;
      margin: 5px;
      border: none;
      background-color: #e0e0e0;
      cursor: pointer;
      border-radius: 5px;
    }
    .emotion-btn.active {
      background-color: #4caf50;
      color: white;
    }
    .result-item {
      margin-bottom: 20px;
    }
    .hidden {
      display: none;
    }
  </style>
</head>
<body>

<h2>🐾 감정 결과 분석</h2>

<!-- ✅ Emotion filter buttons -->
<div id="emotionButtons">
  <button class="emotion-btn" onclick="filterByEmotion('happy')">Happy</button>
  <button class="emotion-btn" onclick="filterByEmotion('relaxed')">Relaxed</button>
  <button class="emotion-btn" onclick="filterByEmotion('sad')">Sad</button>
  <button class="emotion-btn" onclick="filterByEmotion('scared')">Scared</button>
</div>

<hr>

<!-- ✅ Result list -->
<div id="results">
  <c:forEach var="item" items="${analysisList}">
    <div class="result-item" data-emotion="${item.emotionResult}">
      <img src="${item.imagePath}" alt="Pet Image" width="200"><br>
      Emotion: ${item.emotionResult} <br>
      Confidence: ${item.confidence * 100}% <br>
      Analyzed At: ${item.analyzedAt}
    </div>
  </c:forEach>
</div>

<!-- ✅ JavaScript filtering -->
<script>
  function filterByEmotion(emotion) {
    const buttons = document.querySelectorAll('.emotion-btn');
    buttons.forEach(btn => btn.classList.remove('active'));

    const selectedBtn = [...buttons].find(btn => btn.textContent.toLowerCase() === emotion);
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
