<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
    .notification-item { position: relative; padding: 1rem; margin-bottom: .5rem; border: 1px solid #ddd; border-radius: 8px; }
    .notification-meta { font-size: .85rem; color: #888; margin-top: .25rem; }
    .delete-btn { position: absolute; top: .5rem; right: .5rem; background: none; border: none; cursor: pointer; font-size: 1rem; color: #888; }
    .delete-btn:hover { color: #e74c3c; }
    .notification-link { text-decoration: none; color: inherit; display: block; padding-right: 2rem; }
</style>
</br>
<h3>📢 알림함</h3>
<hr/>

<div class="notification-list">
    <c:choose>
        <c:when test="${empty notifications}">
            <p>📭 알림이 없습니다.</p>
        </c:when>
        <c:otherwise>
            <c:forEach var="noti" items="${notifications}">
                <div class="notification-item ${noti.read ? 'read' : 'unread'}" data-id="${noti.id}">
                    <!--  알림 링크/제목 -->
                    <a href="${noti.link}" class="notification-link">
                        <c:out value="${noti.title}" />
                    </a>
                    <!-- 찜한 시각 저장 & time-ago 표시 영역 -->
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

        // --- timeAgo 계산 함수 ---
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

        // --- timeAgo 업데이트 ---
        function updateTimeAgo() {
            document.querySelectorAll('.time-ago').forEach(el => {
                const ms = parseInt(el.dataset.time, 10);
                if (!isNaN(ms)) {
                    el.textContent = calcTimeAgo(ms);
                }
            });
        }
        updateTimeAgo();
        // 1분마다 갱신
        setInterval(updateTimeAgo, 60 * 1000);

        // --- 읽음 처리 + 이동 ---
        document.querySelectorAll('.notification-link').forEach(link => {
            link.addEventListener('click', e => {
                e.preventDefault();
                const item = link.closest('.notification-item');
                const id   = item.dataset.id;
                const url  = link.href;
                fetch(`${cp}/usr/notifications/markAsRead?id=${id}`, { method: 'POST' })
                    .then(() => {
                        item.classList.replace('unread', 'read');
                        window.location.href = url;
                    });
            });
        });

        // --- 삭제 처리 + alert ---
        document.querySelectorAll('.delete-btn').forEach(btn => {
            btn.addEventListener('click', e => {
                e.stopPropagation();
                const item = btn.closest('.notification-item');
                const id   = item.dataset.id;
                if (!confirm('알림을 삭제하시겠습니까?')) return;

                fetch(`${cp}/usr/notifications/delete`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                    body: new URLSearchParams({ id: id })
                })
                    .then(res => res.json())
                    .then(json => {
                        if (json.success) {
                            item.remove();
                            alert(json.msg || '알림이 삭제되었습니다.');
                        } else {
                            alert(json.msg || '삭제에 실패했습니다.');
                        }
                    })
                    .catch(() => {
                        alert('서버 에러로 삭제에 실패했습니다.');
                    });
            });
        });
    });
</script>