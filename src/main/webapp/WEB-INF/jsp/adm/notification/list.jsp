<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
  .notification-item {
    position: relative;
    padding: 1rem;
    margin-bottom: .5rem;
    border: 1px solid #ddd;
    border-radius: 8px;
  }

  .notification-meta {
    font-size: .85rem;
    color: #888;
    margin-top: .25rem;
  }

  .delete-btn {
    position: absolute;
    top: .5rem;
    right: .5rem;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1rem;
    color: #888;
  }

  .delete-btn:hover {
    color: #e74c3c;
  }

  .notification-link {
    text-decoration: none;
    color: inherit;
    display: block;
    padding-right: 2rem;
  }

  .top-controls {
    margin-bottom: 1rem;
    text-align: right;
  }

  .top-controls button {
    background-color: #f44336;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 5px;
    cursor: pointer;
  }

  .top-controls button:hover {
    background-color: #d32f2f;
  }
</style>

<br/>
<h3>📢 관리자 알림함</h3>
<hr/>

<div class="top-controls">
  <button id="deleteAllBtn">전체 삭제</button>
</div>

<div class="notification-list">
  <c:choose>
    <c:when test="${empty notifications}">
      <p>📭 알림이 없습니다.</p>
    </c:when>
    <c:otherwise>
      <c:forEach var="noti" items="${notifications}">
        <div class="notification-item" data-id="${noti.id}">
          <!-- 알림 제목 및 링크 -->
          <a href="${noti.link}" class="notification-link">
            <c:out value="${noti.title}" />
          </a>

          <!-- 등록 시간 -->
          <div class="notification-meta">
            <span class="time-ago" data-time="${noti.regDate.time}">방금 전</span>
          </div>

          <!-- 삭제 버튼 -->
          <button type="button" class="delete-btn" title="삭제">&times;</button>
        </div>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</div>

<script>
  document.addEventListener('DOMContentLoaded', () => {
    const cp = '${pageContext.request.contextPath}';

    // --- 시간 변환 ---
    function calcTimeAgo(ms) {
      const diff = Date.now() - ms;
      const sec = Math.floor(diff / 1000);
      const min = Math.floor(sec / 60);
      const hr  = Math.floor(min / 60);
      const day = Math.floor(hr / 24);

      if (day > 0)    return day + '일 전';
      if (hr > 0)     return hr  + '시간 전';
      if (min > 0)    return min + '분 전';
      if (sec > 5)    return sec + '초 전';
      return '방금 전';
    }

    function updateTimeAgo() {
      document.querySelectorAll('.time-ago').forEach(el => {
        const ms = parseInt(el.dataset.time, 10);
        if (!isNaN(ms)) {
          el.textContent = calcTimeAgo(ms);
        }
      });
    }

    updateTimeAgo();
    setInterval(updateTimeAgo, 60 * 1000);

    // --- 알림 삭제 (개별) ---
    document.querySelectorAll('.delete-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const item = btn.closest('.notification-item');
        const id = item.dataset.id;

        if (!confirm('해당 알림을 삭제하시겠습니까?')) return;

        fetch(`${cp}/adm/notification/delete`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
          body: new URLSearchParams({ id })
        })
                .then(res => res.json())
                .then(json => {
                  if (json.success) {
                    item.remove();
                    alert(json.msg);
                  } else {
                    alert(json.msg || '삭제에 실패했습니다.');
                  }
                })
                .catch(() => alert('서버 오류로 삭제에 실패했습니다.'));
      });
    });

    // --- 알림 전체 삭제 ---
    const deleteAllBtn = document.getElementById('deleteAllBtn');
    deleteAllBtn.addEventListener('click', () => {
      if (!confirm('모든 알림을 삭제하시겠습니까?')) return;

      fetch(`${cp}/adm/notification/deleteAll`, {
        method: 'POST'
      })
              .then(res => res.json())
              .then(json => {
                if (json.success) {
                  document.querySelector('.notification-list').innerHTML = '<p>📭 알림이 없습니다.</p>';
                  alert(json.msg);
                } else {
                  alert(json.msg || '삭제에 실패했습니다.');
                }
              })
              .catch(() => alert('서버 오류로 전체 삭제에 실패했습니다.'));
    });
  });
</script>
