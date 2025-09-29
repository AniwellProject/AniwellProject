<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>내 주변 펫 장소</title>
  <script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=dfd275f49b78960a0458d6f6294cbde2&libraries=services"></script>
  <style>
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: 'SUIT', sans-serif;
      display: flex;
      height: 100vh;
    }
    #map {
      width: 70%;
      height: 100%;
      border-right: 2px solid #ffc0cb;
    }
    #sidebar {
      width: 30%;
      padding: 16px;
      overflow-y: auto;
      background: #fff8f8;
    }
    #filterBtns {
      margin-bottom: 12px;
    }
    #filterBtns button {
      background: #ffdede;
      border: none;
      border-radius: 6px;
      padding: 6px 10px;
      margin-right: 6px;
      margin-bottom: 6px;
      font-weight: bold;
      cursor: pointer;
      transition: all 0.2s;
    }
    #filterBtns button:hover {
      background: #ffc4c4;
    }
    .place-item {
      padding: 12px;
      margin-bottom: 10px;
      background: #fff;
      border-left: 5px solid #ffb6c1;
      border-radius: 6px;
      cursor: pointer;
    }
    .place-item:hover {
      background: #ffeef0;
    }
    .selected {
      background: #ffe0e9;
      outline: 2px solid #ff7f9f;
    }
    .place-details {
      background: #fff0f4;
      border: 1px solid #ffc2d2;
      border-radius: 6px;
      padding: 10px;
      margin-top: 8px;
    }
  </style>
</head>
<body>
<div id="map"></div>

<div id="sidebar">
  <h3>📍 내 주변 펫 장소</h3>
  <div id="filterBtns">
    <button onclick="searchPlaces('애견용품')">🐶 애견용품</button>
    <button onclick="searchPlaces('동물병원')">🏥 동물병원</button>
    <button onclick="searchPlaces('애견카페')">☕ 애견카페</button>
    <button onclick="searchPlaces('공원')">🐾 공원</button>
    <button onclick="searchPlaces('펫호텔')">🏨 펫호텔</button>
  </div>
  <div id="placeList"></div>
</div>

<script>
  let map, currentLocation, markers = [], searchResults = [], currentDetailsId = null;
  const placeListEl = document.getElementById("placeList");

  const pawMarkerImage = new kakao.maps.MarkerImage(
          "/img/paw-marker2.png",
          new kakao.maps.Size(64, 64),
          { offset: new kakao.maps.Point(32, 64) }
  );

  window.onload = function () {
    map = new kakao.maps.Map(document.getElementById("map"), {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 3
    });

    navigator.geolocation.getCurrentPosition(pos => {
      currentLocation = new kakao.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
      map.setCenter(currentLocation);

      new kakao.maps.Circle({
        center: currentLocation, radius: 10,
        strokeWeight: 2, strokeColor: '#ff69b4', strokeOpacity: 0.8,
        fillColor: '#ffb6c1', fillOpacity: 0.7, map: map
      });

      searchPlaces("애견용품"); // 기본 검색
    }, () => alert("📍 위치 정보를 가져올 수 없습니다."));
  };

  function clearMarkers() {
    markers.forEach(m => m.setMap(null));
    markers = [];
  }

  function searchPlaces(keyword) {
    const ps = new kakao.maps.services.Places();
    ps.keywordSearch(keyword, (data, status) => {
      if (status !== kakao.maps.services.Status.OK) {
        placeListEl.innerHTML = "<p>🔍 장소를 찾을 수 없습니다.</p>";
        return;
      }
      searchResults = data;
      renderPlaceList();
    }, { location: currentLocation, radius: 5000 });
  }

  function renderPlaceList() {
    clearMarkers();
    placeListEl.innerHTML = "";

    searchResults.forEach((place, idx) => {
      const pos = new kakao.maps.LatLng(place.y, place.x);

      const marker = new kakao.maps.Marker({
        map: map,
        position: pos,
        title: place.place_name,
        image: pawMarkerImage
      });
      markers.push(marker);

      const item = document.createElement("div");
      item.className = "place-item";
      item.id = "place-" + idx;
      item.innerHTML =
              "<strong>" + place.place_name + "</strong><br>" +
              (place.road_address_name || place.address_name) + "<br>" +
              "📞 " + (place.phone || "없음");

      item.onclick = function () {
        focusPlace(idx);
        map.panTo(pos);
      };

      placeListEl.appendChild(item);

      kakao.maps.event.addListener(marker, "click", function () {
        focusPlace(idx);
        map.panTo(pos);
        document.getElementById("place-" + idx).scrollIntoView({ behavior: "smooth", block: "center" });
      });
    });
  }

  function focusPlace(index) {
    const prev = document.querySelector(".place-item.selected");
    if (prev) {
      prev.classList.remove("selected");
      if (currentDetailsId && document.getElementById(currentDetailsId)) {
        document.getElementById(currentDetailsId).remove();
      }
    }

    const place = searchResults[index];
    const item = document.getElementById("place-" + index);
    item.classList.add("selected");

    const detail = document.createElement("div");
    detail.className = "place-details";
    detail.id = "details-" + index;
    detail.innerHTML =
            "<strong>" + place.place_name + "</strong><br>" +
            "<p>🏠 " + (place.road_address_name || place.address_name) + "</p>" +
            "<p>📞 " + (place.phone || "없음") + "</p>" +
            "<p><a href='" + place.place_url + "' target='_blank'>🔗 카카오에서 상세보기</a></p>";

    item.insertAdjacentElement("afterend", detail);
    currentDetailsId = "details-" + index;
  }
</script>
</body>
</html>
