<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>활동 일지</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/vis/4.21.0/vis.min.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/vis/4.21.0/vis.min.css" rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
    <style>
        body {
            font-family: 'SUIT', sans-serif;
            background: #f2fbf4;
            margin: 0;
            padding: 40px;
            display: flex;
            justify-content: center;
        }

        .popup-container {
            background: #ffffff;
            border-radius: 30px;
            box-shadow: 0 0 25px rgba(190, 220, 170, 0.4);
            padding: 30px;
            max-width: 900px;
            width: 100%;
        }

        h2 {
            margin: 0 0 20px 10px;
            color: #4a6e4d;
            font-size: 24px;
            display: flex;
            align-items: center;
        }

        h2::before {
            content: "🐾";
            margin-right: 8px;
        }

        #timeline {
            height: 420px;
        }
    </style>
</head>
<body>

<div class="popup-container">
    <h2>활동 일지</h2>
    <div id="timeline"></div>
</div>

<script>
    window.addEventListener("DOMContentLoaded", function () {
        var petId = ${petId};
        var rawData = JSON.parse('<c:out value="${activitiesJson}" escapeXml="false"/>');
        var now = new Date();
        var oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000);

        var container = document.getElementById('timeline');
        var items = new vis.DataSet();

        function getEmoji() {
            var list = ["🐱", "💤", "🧸", "😺", "🐾"];
            return list[Math.floor(Math.random() * list.length)];
        }

        function getColor(zoneName) {
            if (zoneName.indexOf("거실") !== -1) return "#fff8b5";
            if (zoneName.indexOf("침실") !== -1) return "#d3f8e2";
            return "#e0eafc";
        }

        function addItem(data) {
            if (!data.enteredAt || !data.exitedAt) return;

            var start = new Date(data.enteredAt.replace('T', ' '));
            var end = new Date(data.exitedAt.replace('T', ' '));
            if (start >= oneDayAgo && end <= now) {
                var color = getColor(data.zoneName);
                items.add({
                    id: Date.now() + Math.random(),
                    content: getEmoji(),
                    start: start,
                    end: end,
                    group: data.zoneName,
                    title: data.zoneName + "에서 " + data.durationSec + "초 동안 머물렀어요 🐾",
                    style: "background-color: " + color + "; border-radius: 16px; font-size:16px; padding:4px;"
                });
            }
        }

        // 초기 데이터 추가
        if (Array.isArray(rawData)) {
            rawData.forEach(function (item) {
                addItem(item);
            });
        }

        var groups = new vis.DataSet([
            { id: "거실", content: "거실" },
            { id: "침실", content: "침실" }
        ]);

        var options = {
            stack: false,
            orientation: 'top',
            margin: { item: 12 },
            showMajorLabels: true,
            showCurrentTime: true,
            min: oneDayAgo,
            max: now,
            tooltip: { followMouse: true },
            zoomMin: 1000 * 60 * 10
        };

        var timeline = new vis.Timeline(container, items, groups, options);

        if (items.length > 0) {
            var latest = items.get().reduce(function(a, b) {
                return new Date(a.start) > new Date(b.start) ? a : b;
            });
            timeline.moveTo(latest.start);
        }


        // WebSocket 연결
        var socket = new SockJS("/ws");
        var stomp = Stomp.over(socket);
        stomp.subscribe("/topic/activity/" + petId, function (msg) {
            var act = JSON.parse(msg.body);
            var start = new Date(act.enteredAt.replace('T', ' '));
            var end = new Date(act.exitedAt.replace('T', ' '));
            var id = Date.now() + Math.random();
            var color = getColor(act.zoneName);

            // ➕ 추가
            items.add({
                id: id,
                content: getEmoji(),
                start: start,
                end: end,
                group: act.zoneName,
                title: act.zoneName + "에서 " + act.durationSec + "초 동안 머물렀어요 🐾",
                style: "background-color: " + color + "; border-radius: 16px; font-size:16px; padding:4px;"
            });

            // ⏩ 추가된 항목으로 이동
            setTimeout(function () {
                timeline.focus(id);
            }, 300);
        });

    });
</script>

</body>
</html>
