# MainProject-ReBit-Android 🌈🦄

# 지구와 나를 살리는 작은 습관 Little, Recycle Habit - Rebit 🌱🌎
## 📌 프로젝트 설명 

Rebit은 다곰이 캐릭터 키우기를 통해 분리수거 지식 습득, 분리수거 방법 확인, 다회용기 포장의 3가지 환경 보호 습관을 기를 수 있도록 기획, 구현된 모바일 게임 서비스입니다.


🍀 <b>용기내</b>: 일회용품이 아닌 다회용기의 손쉬운 사용을 위한 다회용기를 사용하는 상점 지도와 다회용기 측정 서비스 제공<br>
🍀 <b>분리해</b> : 모든 사용자가 손쉽고 올바른 분리수거를 할 수 있도록 쓰레기 인식을 통한 분리수거 방법 제시 및 사용자 위치별 분리수거 정보 제공<br>
🍀 <b>오늘의 팁!</b> : 즐거운 어플 사용 경험과 즐거운 분리수거 경험을 제공<br>

## 📌 Preview
🎥 동영상
/유튜브 링크

## 📌 기능
## Kakao Map API 및 Coroutine을 통한 지도 기능 구현
### 용기내 지도
![0602_ppt_안드로이드_map_marker](https://github.com/DSWU-Unicorn/MainProject-ReBit-Android/assets/71822139/c6b6ccfa-13aa-4698-bc3c-3616bd484434)

🍏 Kakao Map API를 기반으로 기본적인 지도 기능 사용<br>
🍏 나머지는 직접 커스텀해 지도 구현<br>
🍏 서버와 통신하는 값이 많아 UI 스레드가 중단되는 문제를 해결하기 위해 비동기 작업으로 처리할 수 있도록 구현<br>
🍏 Android Jetpack 라이브러리 lifecycleScope.launch을 사용해 Retrofit과 함께 통신<br>
🍏 Lifecycle에 맞춘 컴포넌트 생성 및 제거<br>
🍏 메모리 누수 방지 및 안전한 비동기 처리<br>


## Coroutine을 통한 서버와 비동기 통신-데이터 기반의 상호작용하는 UI 구현
### 용기내 상점 정보 제공
![0602_ppt_안드로이드_서버비동기](https://github.com/DSWU-Unicorn/MainProject-ReBit-Android/assets/71822139/23635dee-edf3-48ff-8125-49dfa2e58532)
📝 상점 정보(이름, 주소, 전화번호, 메뉴) 조회<br>
📝 후기 정보(별점, 리뷰수) 확인 가능<br>

### 리뷰 작성 및 조회
![0602_ppt_안드로이드_리뷰](https://github.com/DSWU-Unicorn/MainProject-ReBit-Android/assets/71822139/39ae9839-0e4a-4a6f-b35b-d705981d7f7c)
📝 리뷰 데이터(별점, 사진, 글) 서버에 저장 및 조회<br>

### 지도 내 검색하기
![용기내지도_검색기능](https://github.com/DSWU-Unicorn/MainProject-ReBit-Android/assets/71822139/655fdeef-d142-4f12-b8f8-33bbcf09be9f)
📝 SearchView를 사용해 Android에서 검색 기능을 구현<br>
📝 setOnQueryTextListener으로 검색창 Event Listener 구현<br>
📝 onQueryTextSubmit으로 검색버튼을 눌렀을때 호출<br>

### 용기내러 가기 기능


https://github.com/DSWU-Unicorn/MainProject-ReBit-Android/assets/71822139/d7e985c7-aec1-4ff1-bd8b-02c30a126038

<시나리오><br>
1. GPS 권한 확인
2. "용기내러 가기" 버튼 클릭 시 가게 주소 반환 api 호출
3. 메인화면 UI 갱신
4. 가게 도착 (반경 250m 이내) -> 다이얼로그 띄어짐
5. 현재위치의 위도, 경도 값과 매장 도로명 주소값을 위도, 경도 값으로 바꾼 값과 거리 계산
6. “예” 버튼 클릭 시 용기내 포인트 증가 api 호출












