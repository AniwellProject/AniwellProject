<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>반려동물 등록</title>
  <style>
    body {
      margin: 0;
      font-family: 'Arial', sans-serif;
    }

    .container {
      max-width: 580px;
      margin: 20px auto;
      background: #fff;
      border-radius: 16px;
      padding: 30px 24px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      position: relative;
    }

    h2 {
      font-size: 22px;
      text-align: center;
      margin-bottom: 24px;
    }

    .form-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20px;
    }

    .photo-area {
      width: 160px;
      height: 160px;
      background: #f1f1f1;
      border-radius: 12px;
      position: relative;
      overflow: hidden;
    }

    .photo-area img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 12px;
    }

    .photo-upload {
      position: absolute;
      bottom: -10px;
      right: -10px;
      background: #333;
      color: white;
      font-size: 16px;
      padding: 8px;
      border-radius: 50%;
      cursor: pointer;
      box-shadow: 0 2px 4px rgba(0,0,0,0.2);
    }

    .info-area {
      width: 100%;
      display: grid;
      grid-template-columns: 80px 1fr;
      gap: 12px 8px;
    }

    label {
      text-align: right;
      padding-top: 6px;
      font-weight: bold;
      font-size: 14px;
    }

    input {
      padding: 6px 8px;
      border: 1px solid #ccc;
      border-radius: 6px;
      font-size: 14px;
    }

    .footer {
      margin-top: 24px;
      display: flex;
      justify-content: center;
    }

    .submit-btn {
      padding: 10px 20px;
      font-size: 14px;
      font-weight: bold;
      border: none;
      border-radius: 10px;
      cursor: pointer;
      background: linear-gradient(to right, #b2e5a6, #87ce8d);
      color: #333;
    }

    input[type="file"] {
      display: none;
    }
  </style>

  <script>
    function previewPhoto(input) {
      const preview = document.getElementById('photo-preview');
      const file = input.files[0];

      if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
          preview.src = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    }
  </script>
</head>
<body>
<div class="container">
  <h2>🐾 반려동물 등록</h2>
  <form action="/usr/pet/doJoin" method="post" enctype="multipart/form-data">
    <div class="form-section">
      <!-- 사진 업로드 -->
      <div class="photo-area">
        <img id="photo-preview" src="/img/default-pet.png" alt="사진" />
        <label class="photo-upload" for="photo">📷</label>
        <input type="file" id="photo" name="photo" accept="image/*" onchange="previewPhoto(this)">
      </div>

      <!-- 입력 항목 -->
      <div class="info-area">
        <label for="name">이름</label>
        <input type="text" id="name" name="name" required />

        <label for="species">종</label>
        <select id="species" name="species" required>
          <option value="">선택</option>
          <option value="강아지">강아지</option>
          <option value="고양이">고양이</option>
        </select>

        <label for="breed">품종</label>
        <input type="text" id="breed" name="breed" required />

        <label for="gender">성별</label>
        <select id="gender" name="gender" required>
          <option value="">선택</option>
          <option value="수컷">수컷</option>
          <option value="암컷">암컷</option>
        </select>

        <label for="birthDate">생일</label>
        <input type="date" id="birthDate" name="birthDate" required />

        <label for="weight">체중</label>
        <input type="number" step="0.1" id="weight" name="weight" required />
      </div>
    </div>

    <!-- 버튼 -->
    <div class="footer">
      <button class="submit-btn" type="submit">등록 완료</button>
    </div>
  </form>
</div>
</body>
</html>
