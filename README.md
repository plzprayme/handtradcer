## 안드로이드 코딩 스타일
[다음](http://developer.gaeasoft.co.kr/development-guide/android-guide/android-coding-style-guide/)과 같은 코딩 스타일을 준수해주세요.

## <기능 명세>

### 1. 로그인 페이지
* O’Auth 
* KAKAO
* FACEBOOK
* 로고 이미지

### 2. GPS
* Material Design
* Animation
* 지도 API
* GPS 건물 IN/OUT

### 3. 손씻기
* 손씻기 포스터
* 손씻기 확인 버튼
*. 푸쉬알람
* 씻은 횟수

### 4. 설정
* GPS, Push Toggle
* 계정정보
* 회원탈퇴
* Fragment Navigation

## TODO

- [ ] 건물안에 있는지 아닌지 테스트하는 버튼 구현하기
  - [ ] 버튼 클릭 시 focusedBuilding() 호출
  - [ ] focusedBuilding() 호출 후 빌딩에 있는지 아닌지 Toast로 출력
- [ ] 지도에 Marker를 찍으려면 어떤 형식의 GPS 데이터가 필요한지 체크하기
- [ ] 위에서 체크한 GPS 데이터 형식들을 String 타입으로 추출하기
- [ ] [LDrawer 적용해보기](https://github.com/keklikhasan/LDrawer)
- [ ] 마커 클러스터링 적용하기
- [ ] 마커간 Polyline 그리기
- [ ] 구글맵 클릭 시 달력 닫히게 하기