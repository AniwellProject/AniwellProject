<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>반려동물 정보 등록</title>
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
      background: #eee center/cover no-repeat;
      border-radius: 12px;
      position: relative;
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
      font-size: 18px;
    }

    #photoInput {
      display: none;
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

    input {
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
  </style>
</head>
<body>
<div class="container">
  <h2>반려동물 정보 등록</h2>
  <form action="/usr/pet/doJoin" method="post" enctype="multipart/form-data">
    <div class="form-section">
      <div class="photo-area" id="photoPreview">
        <label for="photoInput" class="photo-upload">📷</label>
        <input type="file" id="photoInput" name="photo" accept="image/*">
      </div>

      <div class="info-area">
        <label for="name">이름:</label>
        <input type="text" id="name" name="name" required />

        <label for="species">종:</label>
        <input type="text" id="species" name="species" required />

        <label for="breed">품종:</label>
        <input type="text" id="breed" name="breed" required />

        <label for="gender">성별:</label>
        <input type="text" id="gender" name="gender" required />

        <label for="birthDate">생일:</label>
        <input type="date" id="birthDate" name="birthDate" required />

        <label for="weight">체중:</label>
        <input type="number" step="0.1" id="weight" name="weight" required />
      </div>
    </div>

    <div class="footer">
      <button class="submit-btn" type="submit">완료</button>
    </div>
  </form>
</div>

<script>
  var photoInput = document.getElementById('photoInput');
  var photoPreview = document.getElementById('photoPreview');

  photoInput.addEventListener('change', function () {
    var file = this.files[0];
    if (!file) return;

    var reader = new FileReader();
    reader.onload = function (e) {
      photoPreview.style.backgroundImage = "url('" + e.target.result + "')";
    };
    reader.readAsDataURL(file);
  });
</script>
</body>
</html>
