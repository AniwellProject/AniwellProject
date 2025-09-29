<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>회원가입</title>
  <style>
    body {
      background-color: #999;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      margin: 0;
      font-family: 'Arial';
    }

    .signup-wrapper {
      width: 700px;
      max-height: 90vh;   /* ✅ 높이 제한 */
      background: white;
      border-radius: 15px;
      display: flex;
      overflow: hidden;
      box-shadow: 0 10px 20px rgba(0,0,0,0.2);
    }

    .left-panel {
      width: 40%;
      background: linear-gradient(135deg, #f0eb94, #bdd1c6);
      display: flex;
      align-items: flex-end;
      justify-content: center;
      padding: 20px;
    }

    .left-panel button {
      background-color: #f5d76e;
      border: none;
      padding: 10px 20px;
      border-radius: 10px;
      cursor: pointer;
      font-weight: bold;
    }

    .form-panel {
      width: 60%;
      padding: 30px;
      position: relative;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      overflow-y: auto;  /* ✅ 스크롤 생기도록 */
    }

    .form-step {
      display: none;
      flex-direction: column;
      gap: 12px;
      animation: fadeIn 0.3s ease-in;
    }

    .form-step.active {
      display: flex;
    }

    input, select {
      border: none;
      border-bottom: 1px solid #aaa;
      background: transparent;
      padding: 8px;
      outline: none;
    }

    button.next-button, button.prev-button, button.submit-button {
      background-color: #a8cbb5;
      border: none;
      padding: 10px;
      border-radius: 10px;
      margin-top: 10px;
      cursor: pointer;
      font-weight: bold;
    }

    .logo-img {
      width: 80px;
      margin-top: 20px;
      display: block;
      margin-left: auto;
      margin-right: auto;
    }

    .timeline {
      margin-top: 20px;
      display: flex;
      justify-content: center;
      gap: 10px;
    }

    .paw-icon {
      width: 24px;
    }

    @keyframes fadeIn {
      from {opacity: 0; transform: translateY(10px);}
      to {opacity: 1; transform: translateY(0);}
    }

    .error-message {
      color: red;
      text-align: center;
      font-size: 13px;
      margin-bottom: 10px;
    }
  </style>

  <script>
    function goToStep(step) {
      document.getElementById('step1').classList.remove('active');
      document.getElementById('step2').classList.remove('active');
      document.getElementById('step' + step).classList.add('active');

      // 발바닥 이미지 교체
      document.getElementById('paw1').src = step === 1 ? '/img/paw_active.png' : '/img/paw_inactive.png';
      document.getElementById('paw2').src = step === 2 ? '/img/paw_active.png' : '/img/paw_inactive.png';
    }

    function updateAuthLevel() {
      const auth = document.querySelector('[name="authName"]').value;
      document.getElementById('authLevel').value = auth === '수의사' ? 9 : 1;
    }

    function validateForm() {
      const phone = document.querySelector('[name="cellphone"]');
      const phonePattern = /^\d{3}-\d{3,4}-\d{4}$/;
      if (!phonePattern.test(phone.value)) {
        alert("전화번호 형식이 올바르지 않습니다. 예: 000-0000-0000");
        return false;
      }
      return true;
    }
  </script>
</head>
<body>

<div class="signup-wrapper">
  <!-- 왼쪽 패널 -->
  <div class="left-panel">
    <button onclick="location.href='/usr/member/login'">sign up</button>
  </div>

  <!-- 오른쪽 패널 -->
  <form class="form-panel" action="/usr/member/doJoin" method="post" onsubmit="return validateForm()">
    <c:if test="${param.error != null}">
      <div class="error-message">${param.error}</div>
    </c:if>

    <!-- STEP 1 -->
    <div class="form-step active" id="step1">
      <h2>Create Account</h2>
      <input type="text" name="loginId" placeholder="ID" required>
      <input type="password" name="loginPw" placeholder="PW" required>
      <input type="text" name="name" placeholder="NAME" required>
      <input type="text" name="nickname" placeholder="NICKNAME" required>
      <button type="button" class="next-button" onclick="goToStep(2)">다음</button>
    </div>

    <!-- STEP 2 -->
    <div class="form-step" id="step2">
      <h2>Contact Info</h2>
      <input type="text" name="cellphoneNum" placeholder="PHONE NUMBER (000-0000-0000)" required>
      <input type="email" name="email" placeholder="EMAIL" required>
      <input type="text" name="address" placeholder="ADDRESS" required>

      <select name="authName" onchange="updateAuthLevel()" required>
        <option value="일반">일반</option>
        <option value="수의사">수의사</option>
      </select>
      <input type="hidden" id="authLevel" name="authLevel" value="1">

      <button type="button" class="prev-button" onclick="goToStep(1)">뒤로가기</button>
      <button type="submit" class="submit-button">sign up</button>
    </div>

    <!-- 발바닥 타임라인 -->
    <div class="timeline">
      <img id="paw1" class="paw-icon" src="/img/paw_active.png">
      <img id="paw2" class="paw-icon" src="/img/paw_inactive.png">
    </div>

    <!-- 로고 -->
    <img class="logo-img" src="/img/logo.png" alt="Aniwell Logo">
  </form>
</div>

</body>
</html>
