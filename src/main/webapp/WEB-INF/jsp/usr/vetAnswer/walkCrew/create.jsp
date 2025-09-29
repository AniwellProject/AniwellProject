<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<title>크루 등록</title>

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

button[type="submit"] {
	margin-top: 20px;
	padding: 10px 20px;
	background-color: #4CAF50;
	color: white;
	border: none;
	border-radius: 5px;
	cursor: pointer;
}

.dong-list {
	margin-top: 10px;
}

.dong-list button {
	margin: 5px;
	padding: 5px 10px;
}
</style>
</head>
<body>

	<form action="/usr/walkCrew/doCreate" method="post">
		<h2>🚀 새 크루 등록</h2>

		<label>제목</label>
		<input type="text" name="title" required />

		<label>설명</label>
		<textarea name="description" rows="5" required></textarea>

		<label>동 선택</label>
		<div>
			현재 위치:
			<span id="currentLocation">확인 중...</span>
		</div>

		<div class="dong-list" id="dongListContainer"></div>

		<input type="hidden" name="selectedDong" id="selectedDong" />
		<input type="hidden" name="districtId" id="districtIdInput" />

		<button type="submit">등록</button>
	</form>

	<div style="text-align: center; margin-top: 30px;">
		<a href="/usr/walkCrew/list">← 목록으로 돌아가기</a>
	</div>

	<script>
	  let currentCity = '';
	  let currentDistrict = '';

	  function loadKakaoMap(callback) {
	    const script = document.createElement("script");
	    script.src = "https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJsKey}&autoload=false&libraries=services";
	    script.onload = () => {
	      kakao.maps.load(callback);
	    };
	    document.head.appendChild(script);
	  }

	  window.onload = function () {
	    loadKakaoMap(function () {
	      if (navigator.geolocation) {
	        navigator.geolocation.getCurrentPosition(function (position) {
	          const lat = position.coords.latitude;
	          const lng = position.coords.longitude;

	          const geocoder = new kakao.maps.services.Geocoder();
	          geocoder.coord2RegionCode(lng, lat, function (result, status) {
	            if (status === kakao.maps.services.Status.OK) {
	              for (let i = 0; i < result.length; i++) {
	                if (result[i].region_type === "B") {
	                  const fullAddr = result[i].address_name;
	                  document.getElementById("currentLocation").innerText = fullAddr;

	                  const parts = fullAddr.split(" ");
	                  if (parts.length >= 3) {
	                    currentCity = parts[0];
	                    currentDistrict = parts[1];
	                    loadDongList(currentCity, currentDistrict);
	                  }
	                  break;
	                }
	              }
	            } else {
	              document.getElementById("currentLocation").innerText = "위치 정보 불러오기 실패";
	            }
	          });
	        }, function (error) {
	          document.getElementById("currentLocation").innerText = "위치 접근 거부됨";
	        });
	      } else {
	        document.getElementById("currentLocation").innerText = "GPS를 지원하지 않는 브라우저입니다.";
	      }
	    });
	  };

	  function loadDongList(city, district) {
	    const url = "/usr/walkCrew/getDongs?city=" + encodeURIComponent(city) + "&district=" + encodeURIComponent(district);

	    fetch(url)
	      .then(response => response.json())
	      .then(data => {
	        const container = document.getElementById("dongListContainer");
	        container.innerHTML = "";

	        if (data.length === 0) {
	          container.innerText = "해당 지역의 동 정보가 없습니다.";
	          return;
	        }

	        data.forEach(dong => {
	          const btn = document.createElement("button");
	          btn.type = "button";
	          btn.innerText = dong;
	          btn.onclick = () => {
	            document.getElementById("selectedDong").value = dong;

	            document.querySelectorAll(".dong-list button").forEach(b => {
	              b.style.backgroundColor = "";
	            });
	            btn.style.backgroundColor = "#ddd";

	            // ✅ 수정된 부분: city + district + dong 모두 포함
	            fetch("/usr/walkCrew/getDistrictId?city=" + encodeURIComponent(currentCity)
	                + "&district=" + encodeURIComponent(currentDistrict)
	                + "&dong=" + encodeURIComponent(dong))
	              .then(response => response.text())
	              .then(districtId => {
	                document.getElementById("districtIdInput").value = districtId;
	              });
	          };
	          container.appendChild(btn);
	        });
	      })
	      .catch(err => {
	        document.getElementById("dongListContainer").innerText = "동 정보 로딩 실패";
	        console.error("Error loading dongs:", err);
	      });
	  }
	</script>

</body>
</html>
