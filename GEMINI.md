
### 코드 컨벤션 
/Users/kim-yejin/Gachigage_BE/CONTRIBUTING.md

해당 경로 md 파일에 공통 코드 컨벤션을 기본적으로 준수한다. 


### PRODUCT API 명세  

🏠 홈 - 상품 리스트 조회  
GET /products

Headers  
```Authorization: Bearer <token>```

Query Params

| 이름        | 타입     | 설명           |
| --------- | ------ | ------------ |
| type      | string | 전체 조회(all) 등 |
| category  | string | 카테고리 필터      |
| itemType  | string | 아이템 타입       |
| minPrice  | number | 최소 가격        |
| maxPrice  | number | 최대 가격        |
| province  | string | 도            |
| city      | string | 시            |
| district  | string | 구            |
| tradeType | string | 거래 유형        |
| group     | string | 전체 / 일괄 / 개별 |
| page      | number | 페이지 번호       |
| size      | number | 페이지 크기       |


Response  
```json
{
  "type": "all",
  "page": 1,
  "size": 10,
  "items": [
    {
      "productId": 111,
      "title": "Lorem ipsum",
      "minPrice": 150000,
      "maxPrice": 180000,
      "thumbnailUrl": "https://bucket/img1.jpg",
      "category": "식기류",
      "province": "서울특별시",
      "city": "강남구",
      "district": "역삼동",
      "tradeType": "직거래",
      "viewCount": 32,
      "createdAt": "2024-01-10T12:30:00"
    }
  ]
}

```


상품 등록
POST /products

Headers 
```Authorization: Bearer <token>```

Request Body
```json
{
  "category": {
    "main": "업종명",
    "sub": "세부항목명"
  },
  "title": "제목을 입력해주세요",
  "detail": "세부 정보를 입력해주세요",
  "priceTable": [
    { "minQuantity": 1, "price": 10000 },
    { "minQuantity": 5, "price": 45000 }
  ],
  "tradeTypes": ["직거래", "택배거래"],
  "preferredTradeLocations": [
    {
      "latitude": 37.497951,
      "longitude": 127.027619,
      "address": "서울 강남구 역삼동 강남역"
    },
    {
      "latitude": 37.503435,
      "longitude": 127.048928,
      "address": "서울 강남구 선릉역"
    }
  ],
  "imageUrls": [
    "https://bucket/image1.jpg",
    "https://bucket/image2.jpg"
  ]
}
```

Response
```json
{
  "productId": 111,
  "message": "상품이 성공적으로 등록되었습니다."
}

```

Location Header
```json
Location: /products/{productId}
```


상품 수정
PUT /products/{productId}

Headers
```Authorization: Bearer <token>```

Request Body
```json
{
  "category": {
    "main": "업종명",
    "sub": "세부항목명"
  },
  "title": "수정된 제목",
  "detail": "수정된 상세 설명",
  "priceTable": [
    { "minQuantity": 1, "price": 10000 },
    { "minQuantity": 5, "price": 45000 }
  ],
  "tradeTypes": ["직거래"],
  "preferredTradeLocations": [
    {
      "latitude": 37.497951,
      "longitude": 127.027619,
      "address": "서울 강남구 강남역"
    },
    {
      "latitude": 37.503435,
      "longitude": 127.048928,
      "address": "서울 강남구 선릉역"
    }
  ],
  "imageUrls": [
    "https://bucket/image1.jpg",
    "https://bucket/image2.jpg"
  ]
}

```

Response
```json
{
  "productId": 111,
  "message": "상품이 성공적으로 수정되었습니다."
}

```


Location Header
```json
Location: /products/111
```


상품 삭제  
DELETE /products/{productId}

Headers 
```json
Authorization: Bearer <token>
```

Response
```json
{
"productId": 111,
"message": "상품이 정상적으로 삭제되었습니다."
}
```

상품 상세 조회  
GET /products/{productId}


Headers  
```
Authorization: Bearer <token>
```



Response
```json
{
"productId": 111,
"title": "상품 제목",
"detail": "물품 상세 설명",
"sellerName": "홍길동",
"category": {
"main": "업종명",
"sub": "세부항목명"
},
"tradeTypes": ["직거래", "택배거래"],
"imageUrls": [
"https://bucket/image1.jpg",
"https://bucket/image2.jpg"
],
"priceTable": [
{ "minQuantity": 1, "price": 10000 },
{ "minQuantity": 5, "price": 45000 }
],
"preferredTradeLocations": [
{
"latitude": 37.497951,
"longitude": 127.027619,
"address": "서울 강남구 강남역"
}
],
"viewCount": 52,
"likeCount": 13,
"isLiked": true,
"relatedProducts": [
{
"productId": 222,
"title": "비슷한 상품",
"thumbnailUrl": "https://bucket/related1.jpg",
"minPrice": 8000,
"maxPrice": 20000
}
]
}
```


좋아요 추가
```
POST /products/{productId}/likes
```

Headers
```
Authorization: Bearer <token>
```


좋아요 취소  
DELETE /products/{productId}/likes

Headers  
```
Authorization: Bearer <token>
```
