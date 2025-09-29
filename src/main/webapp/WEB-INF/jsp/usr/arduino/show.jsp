<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>아두이노 실시간 데이터</title>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script>
    function loadData() {
      $.get("/usr/arduino/api/data", function(data) {
        document.getElementById("value").innerText = data;
      });
    }

    setInterval(loadData, 1000); // 1초마다 요청
    window.onload = loadData;
  </script>
</head>
<body>
<h1>💡 아두이노 센서값:</h1>
<h2 id="value" style="color:blue">로딩 중...</h2>
</body>
</html>
