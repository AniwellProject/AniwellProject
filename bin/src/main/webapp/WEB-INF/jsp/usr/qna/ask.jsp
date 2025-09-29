<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>질문 등록</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.1.4/tailwind.min.css">
</head>
<body class="bg-gray-100 p-8">
<div class="max-w-3xl mx-auto bg-white p-6 rounded shadow">
  <h1 class="text-xl font-bold mb-4">Q&A 질문 등록</h1>
  <form id="askForm">
    <div class="mb-4">
      <label class="block mb-1 font-semibold">제목</label>
      <input type="text" name="title" class="w-full border p-2 rounded" required>
    </div>
    <div class="mb-4">
      <label class="block mb-1 font-semibold">내용</label>
      <textarea name="body" class="w-full border p-2 rounded h-40" required></textarea>
    </div>
    <div class="mb-4">
      <label>
        <input type="checkbox" name="isSecret">
        비공개로 등록
      </label>
    </div>
    <div>
      <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded">등록</button>
    </div>
  </form>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
  $('#askForm').on('submit', function (e) {
    e.preventDefault();

    const formData = $(this).serialize();

    $.post('/usr/qna/doAsk', formData, function (data) {
      if (data.resultCode.startsWith('S-')) {
        alert(data.msg);
        location.href = '/usr/qna/list';
      } else {
        alert(data.msg);
      }
    });
  });
</script>
</body>
</html>
