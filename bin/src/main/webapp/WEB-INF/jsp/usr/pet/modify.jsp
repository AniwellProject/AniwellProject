<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>반려동물 정보 수정</title>
  <style>
    body {
      margin: 0;
      background: #ccc;
      font-family: 'Arial';
    }

    .container {
      width: 700px;
      margin: 40px auto;
      background: white;
      border-radius: 20px;
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
      padding: 30px 40px;
      position: relative;
    }

    .container::before {
      content: "";
      position: absolute;
      top: 10px;
      left: 10px;
      width: 100%;
      height: 100%;
      background: #f4df8f;
      border-radius: 20px;
      z-index: -1;
    }

    h2 {
      margin-bottom: 20px;
    }

    .form-section {
      display: flex;
      align-items: flex-start;
      gap: 30px;
    }

    .photo-area {
      width: 200px;
      height: 200px;
      background: #eee;
      border-radius: 12px;
      position: relative;
      overflow: hidden;
    }

    .photo-area img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .photo-upload {
      position: absolute;
      right: -10px;
      bottom: -10px;
      background: #333;
      color: white;
      padding: 8px;
      border-radius: 50%;
      cursor: pointer;
    }

    .info-area {
      flex: 1;
      display: grid;
      grid-template-columns: 100px 1fr;
      row-gap: 10px;
      column-gap: 10px;
    }

    label {
      text-align: right;
      padding-top: 8px;
    }

    input, textarea {
      padding: 6px;
      border: 1px solid #ccc;
      border-radius: 6px;
      width: 100%;
    }

    .footer {
      margin-top: 20px;
      text-align: center;
    }

    .submit-btn {
      padding: 10px 30px;
      border: none;
      background: linear-gradient(to right, #d4e0a2, #a9d57c);
      border-radius: 12px;
      font-weight: bold;
      cursor: pointer;
    }

    .delete-btn {
      padding: 10px 30px;
      margin-left: 20px;
      border: none;
      background: linear-gradient(to right, #f9a8a8, #f87171);
      color: white;
      border-radius: 12px;
      font-weight: bold;
      cursor: pointer;
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

    function confirmDelete() {
      if (confirm("정말 삭제하시겠어요? 🐾")) {
        const petId = document.querySelector('input[name="petId"]').value;
        location.href = '/usr/pet/delete?petId='+petId;
      }
    }
  </script>
</head>
<body>
<div class="container">
  <h2>반려동물 정보 수정</h2>
  <form action="/usr/pet/doModify" method="post" enctype="multipart/form-data">
    <input type="hidden" name="petId" value="${pet.id}" />

    <div class="form-section">
      <div class="photo-area">
        <img id="photo-preview" src="${pet.photo != null ? pet.photo : '/img/default-pet.png'}" alt="사진" />
        <label class="photo-upload" for="photo">📷</label>
        <input type="file" id="photo" name="photo" accept="image/*" onchange="previewPhoto(this)">
      </div>
      <div class="info-area">
        <label for="name">이름:</label>
        <input type="text" id="name" name="name" value="${pet.name}" required />

        <label for="species">종:</label>
        <input type="text" id="species" name="species" value="${pet.species}" required />

        <label for="breed">품종:</label>
        <input type="text" id="breed" name="breed" value="${pet.breed}" required />

        <label for="gender">성별:</label>
        <input type="text" id="gender" name="gender" value="${pet.gender}" required />

        <label for="birthDate">생일:</label>
        <input type="date" id="birthDate" name="birthDate" value="${pet.birthDate}" required />

        <label for="weight">체중:</label>
        <input type="number" step="0.1" id="weight" name="weight" value="${pet.weight}" required />
      </div>
    </div>

    <div class="footer">
      <button class="submit-btn" type="submit">수정 완료</button>
      <button class="delete-btn" type="button" onclick="confirmDelete()">삭제</button>
    </div>
  </form>
</div>
</body>
</html>
