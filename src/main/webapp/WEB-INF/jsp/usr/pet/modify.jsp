<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>반려동물 수정</title>
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
      gap: 16px;
    }

    .submit-btn, .delete-btn {
      padding: 10px 20px;
      font-size: 14px;
      font-weight: bold;
      border: none;
      border-radius: 10px;
      cursor: pointer;
    }

    .submit-btn {
      background: linear-gradient(to right, #b2e5a6, #87ce8d);
      color: #333;
    }

    .delete-btn {
      background: linear-gradient(to right, #fca5a5, #f87171);
      color: white;
    }

    input[type="file"] {
      display: none;
    }
  </style>

</head>
<body>
<div class="container">
  <h2>🐾 반려동물 정보 수정</h2>
  <form action="/usr/pet/doModify" method="post" enctype="multipart/form-data">
    <input type="hidden" name="petId" value="${pet.id}" />

    <div class="form-section">
      <!-- 사진 영역 -->
      <div class="photo-area">
        <img id="photo-preview" src="${pet.photo != null ? pet.photo : '/img/default-pet.png'}" alt="사진" />
        <label class="photo-upload" for="photo">📷</label>
        <input type="file" id="photo" name="photo" accept="image/*" onchange="previewPhoto(this)">
      </div>

      <!-- 입력 폼 -->
      <div class="info-area">
        <label for="name">이름</label>
        <input type="text" id="name" name="name" value="${pet.name}" required />

        <label for="species">종</label>
        <input type="text" id="species" name="species" value="${pet.species}" required />

        <label for="breed">품종</label>
        <input type="text" id="breed" name="breed" value="${pet.breed}" required />

        <label for="gender">성별</label>
        <input type="text" id="gender" name="gender" value="${pet.gender}" required />

        <label for="birthDate">생일</label>
        <input type="date" id="birthDate" name="birthDate" value="${pet.birthDate}" required />

        <label for="weight">체중</label>
        <input type="number" step="0.1" id="weight" name="weight" value="${pet.weight}" required />
      </div>
    </div>

    <!-- 하단 버튼 -->
    <div class="footer">
      <button class="submit-btn" type="submit">수정 완료</button>
      <button class="delete-btn" type="button" onclick="confirmDelete()">삭제</button>
    </div>
  </form>
</div>
</body>
</html>
