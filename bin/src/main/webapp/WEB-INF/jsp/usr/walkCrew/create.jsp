<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<title>크루 등록</title>

<!-- Kakao Maps JS SDK (키 바인딩 확인) -->
<script src="http://dapi.kakao.com/v2/maps/sdk.js?appkey=e168f5867f0ad1b66e9692a214050110&libraries=services"></script>


<style>
form {
	width: 60%;
	margin: 30px auto;
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
}

label {
	font-weight: bold;
	display: block;
	margin-top: 15px;
}

input[type="text"], textarea {
	width: 100%;
	padding: 10px;
	margin-top: 5px;
}

.dong-list button {
	margin: 5px;
	padding: 5px 10px;
}

button[type="submit"] {
	margin-top: 20px;
	padding: 10px 20px;
	background-color: #4CAF50;
	color: white;
	border: none;
	border-radius: 5px;
}
</style>

<script>
        // 페이지 로딩 시 현재 위치로 구(district) → 동 리스트 자동 호출
        window.onload = () => {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(function(position) {
                    const lat = position.coords.latitude;
                    const lng = position.coords.longitude;

                    const geocoder = new kakao.maps.services.Geocoder();
                    geocoder.coord2RegionCode(lng, lat, function(result, status) {
                        if (status === kakao.maps.services.Status.OK) {
                            const region = result.find(r => r.region_type === "H");
                            const district = region.region_2depth_name;

                            fetch(`/usr/api/dongList?district=${district}`)
                                .then(res => res.json())
                                .then(data => {
                                    const dongListDiv = document.querySelector("#dongList");
                                    dongListDiv.innerHTML = "";

                                    if (data.length === 0) {
                                        dongListDiv.innerHTML = "동 정보가 없습니다.";
                                        return;
                                    }

                                    data.forEach(dong => {
                                        const btn = document.createElement("button");
                                        btn.type = "button";
                                        btn.textContent = dong;
                                        btn.onclick = () => {
                                            document.querySelector("#selectedDong").value = dong;
                                            document.querySelectorAll("#dongList button").forEach(b => {
                                                b.style.backgroundColor = "";
                                                b.style.color = "";
                                            });
                                            btn.style.backgroundColor = "#4CAF50";
                                            btn.style.color = "white";
                                        };
                                        dongListDiv.appendChild(btn);
                                    });
                                });
                        }
                    });
                }, function(err) {
                    console.warn("위치 접근 실패:", err);
                });
            } else {
                alert("이 브라우저는 위치 정보를 지원하지 않습니다.");
            }
        };
    </script>
</head>
<body>

	<h2 style="text-align: center;">🚀 새 크루 등록</h2>

	<form action="/usr/walkCrew/doCreate" method="post">

		<label for="title">제목</label>
		<input type="text" name="title" id="title" required />

		<label for="descriptoin">설명</label>
		<textarea name="descriptoin" id="descriptoin" rows="5" required></textarea>

		<!-- 동 선택 -->
		<label>동 선택</label>
		<div class="dong-list" id="dongList">
			<small>현재 위치를 기반으로 동네를 불러옵니다</small>
		</div>
		<input type="hidden" name="dong" id="selectedDong" />

		<input type="hidden" name="leaderId" value="${rq.loginedMemberId}" />

		<button type="submit">등록</button>
	</form>

	<div style="text-align: center;">
		<a href="/usr/walkCrew/list">← 목록으로 돌아가기</a>
	</div>

</body>
</html>
