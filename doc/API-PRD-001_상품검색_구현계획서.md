# API-PRD-001: 상품 검색 구현 계획서

## 1. API 명세 요약

| 항목 | 값 |
|------|-----|
| **API ID** | API-PRD-001 |
| **HTTP Method** | GET |
| **Endpoint** | `/api/v1/products/search` |
| **인증** | Bearer Token (JWT) |
| **권한** | CUSTOMER |
| **관련 UC** | UC-C04 (상품 검색) |

### Request Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|-------|------|
| `keyword` | String | **필수** | - | 검색어 (2~8자) |
| `latitude` | Double | **필수** | - | 배송지 위도 |
| `longitude` | Double | **필수** | - | 배송지 경도 |
| `categoryId` | Long | 선택 | - | 카테고리 필터 |
| `sort` | String | 선택 | `recommended` | 정렬 옵션 |
| `page` | Integer | 선택 | 0 | 페이지 번호 |
| `size` | Integer | 선택 | 20 | 페이지 크기 |

### 정렬 옵션
- `recommended` - 추천순 (기본값)
- `newest` - 최신순
- `sales` - 판매량순
- `priceAsc` - 가격 낮은순
- `priceDesc` - 가격 높은순

### Response
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "productId": 101,
        "productName": "유기농 사과 (3입)",
        "price": 12000,
        "salePrice": 9900,
        "discountRate": 18,
        "imageUrl": "https://...",
        "stock": 25,
        "storeName": "행복한 마트",
        "storeId": 1,
        "distance": 1.2
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 35,
    "totalPages": 2
  }
}
```

---

## 2. 현재 상태 분석

### 존재하는 파일
| 파일 | 상태 |
|------|------|
| `Product.java` (엔티티) | ✅ 존재 |
| `Category.java` (엔티티) | ✅ 존재 |
| `StoreRepository.java` | ✅ 존재 (거리 검색 메서드 포함) |

### 생성해야 할 파일
| 파일 | 설명 |
|------|------|
| `ProductRepository.java` | 상품 검색 쿼리 |
| `CategoryRepository.java` | 카테고리 조회 |
| `ProductSearchService.java` | 검색 비즈니스 로직 |
| `ProductSearchController.java` | REST API 엔드포인트 |
| `ProductSearchRequest.java` | 검색 요청 DTO |
| `ProductSearchResponse.java` | 검색 응답 DTO |
| `ProductSortType.java` | 정렬 옵션 Enum |

---

## 3. 구현 계획

### 3.1 Enum 생성
**파일:** `src/main/java/com/example/finalproject/product/enums/ProductSortType.java`

```java
public enum ProductSortType {
    RECOMMENDED,  // 추천순
    NEWEST,       // 최신순
    SALES,        // 판매량순
    PRICE_ASC,    // 가격 낮은순
    PRICE_DESC    // 가격 높은순
}
```

### 3.2 DTO 생성

**Request DTO:** `src/main/java/com/example/finalproject/product/dto/request/ProductSearchRequest.java`
- keyword: @NotBlank, @Size(min=2, max=8)
- latitude: @NotNull
- longitude: @NotNull
- categoryId: Long (선택)
- sort: ProductSortType (기본값: RECOMMENDED)

**Response DTO:** `src/main/java/com/example/finalproject/product/dto/response/ProductSearchResponse.java`
- productId, productName, price, salePrice, discountRate
- imageUrl, stock, storeName, storeId, distance

### 3.3 Repository 생성

**ProductRepository:** `src/main/java/com/example/finalproject/product/repository/ProductRepository.java`

핵심 쿼리 - pg_trgm + PostGIS 활용:
```sql
SELECT p.*, s.store_name,
       ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) / 1000 as distance
FROM products p
JOIN stores s ON p.store_id = s.id
WHERE p.is_active = true
  AND s.status = 'APPROVED'
  AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 3000)
  AND p.product_name ILIKE '%' || :keyword || '%'
  AND (:categoryId IS NULL OR p.category_id = :categoryId)
ORDER BY [정렬조건]
```

**CategoryRepository:** `src/main/java/com/example/finalproject/product/repository/CategoryRepository.java`

### 3.4 Service 생성
**파일:** `src/main/java/com/example/finalproject/product/service/ProductSearchService.java`

주요 로직:
1. 검색어 유효성 검증
2. 위치 기반 배달 가능 마트 필터링 (3km 반경)
3. 키워드 매칭 상품 검색 (ILIKE)
4. 카테고리 필터 적용 (선택)
5. 정렬 적용
6. 페이징 처리
7. 거리 정보 포함 응답 반환

### 3.5 Controller 생성
**파일:** `src/main/java/com/example/finalproject/product/controller/ProductSearchController.java`

```java
@GetMapping("/search")
public ResponseEntity<ApiResponse<Page<ProductSearchResponse>>> searchProducts(
    @Valid @ModelAttribute ProductSearchRequest request,
    @PageableDefault(size = 20) Pageable pageable
)
```

---

## 4. 파일 생성 순서

| 순서 | 파일 | 경로 |
|------|------|------|
| 1 | ProductSortType.java | `.../product/enums/` |
| 2 | ProductSearchRequest.java | `.../product/dto/request/` |
| 3 | ProductSearchResponse.java | `.../product/dto/response/` |
| 4 | CategoryRepository.java | `.../product/repository/` |
| 5 | ProductRepository.java | `.../product/repository/` |
| 6 | ProductSearchService.java | `.../product/service/` |
| 7 | ProductSearchController.java | `.../product/controller/` |
| 8 | ErrorCode 추가 | `.../global/exception/ErrorCode.java` |

---

## 5. 핵심 기술 포인트

### 5.1 pg_trgm을 활용한 검색
- DDL에 이미 GIN 인덱스 정의됨: `idx_products_name_trgm`
- ILIKE 검색으로 부분 문자열 매칭

### 5.2 PostGIS 거리 계산
- `ST_DWithin()` - 3km 반경 내 마트 필터링
- `ST_Distance()` - 정확한 거리 계산 (km 단위로 변환)

### 5.3 동적 정렬
- RECOMMENDED: 기본 순서 (id)
- NEWEST: `created_at DESC`
- SALES: `order_count DESC`
- PRICE_ASC: `COALESCE(sale_price, price) ASC`
- PRICE_DESC: `COALESCE(sale_price, price) DESC`

---

## 6. 검증 방법

### 6.1 컴파일 테스트
```bash
./gradlew compileJava
```

### 6.2 API 호출 테스트
```bash
curl -X GET "http://localhost:8080/api/v1/products/search?keyword=사과&latitude=37.4979&longitude=127.0276&page=0&size=20" \
  -H "Authorization: Bearer {token}"
```

### 6.3 예상 결과
- 검색어 "사과"가 포함된 상품 목록 반환
- 위치 기준 3km 내 마트의 상품만 포함
- 거리(distance) 정보 포함
- 페이징 정보 포함

---

## 7. 에러 처리

| 상황 | HTTP Status | ErrorCode |
|------|------------|-----------|
| 검색어 2자 미만 | 400 | INVALID_KEYWORD_LENGTH |
| 검색어 8자 초과 | 400 | INVALID_KEYWORD_LENGTH |
| 필수 파라미터 누락 | 400 | INVALID_INPUT_VALUE |
| 검색 결과 없음 | 200 | 빈 배열 반환 (정상) |

---

## 8. Store 엔티티의 위치 정보 (Point 타입 설명)

### Q: Store 엔티티에 위도/경도 필드가 없는데?

**A:** Store 엔티티에는 `Point location` 필드가 있습니다.

```java
// Store.java (72-73번 줄)
@Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
private Point location;  // org.locationtech.jts.geom.Point
```

### 좌표 저장 방식 비교

| 방식 | 필드 구조 | 장점 | 단점 |
|------|----------|------|------|
| **일반 방식** | `Double latitude`, `Double longitude` | 직관적, 단순 | 공간 쿼리/인덱스 불가 |
| **PostGIS 방식** | `Point location` | GIST 인덱스, 거리 계산, 반경 검색 가능 | 학습 필요 |

### Point 타입 이해하기

```java
// Point는 위도(Y)와 경도(X)를 하나의 객체로 저장
Point location;

// 좌표 추출
double longitude = location.getX();  // 경도 (X축)
double latitude = location.getY();   // 위도 (Y축)

// Point 생성 (Java 코드)
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.Coordinate;

GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
Point point = factory.createPoint(new Coordinate(longitude, latitude));
// 주의: Coordinate(X, Y) = Coordinate(경도, 위도) 순서!
```

### SRID 4326이란?

- **SRID**: Spatial Reference System Identifier (공간 참조 시스템 식별자)
- **4326**: WGS 84 좌표계 (GPS에서 사용하는 세계 표준)
- 위도: -90 ~ +90 (적도 기준)
- 경도: -180 ~ +180 (본초 자오선 기준)

### SQL에서 Point 다루기

```sql
-- Point 생성
ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326)
-- ST_MakePoint(경도, 위도) 순서!

-- 거리 계산 (미터 단위)
ST_Distance(
    s.location,
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
)

-- 반경 검색 (3000m = 3km)
ST_DWithin(
    s.location,
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
    3000
)
```

---

## 9. PostGIS & JTS 학습 가이드

### 9.1 학습 로드맵

```
[1단계: 기초 개념]
    ↓
[2단계: PostGIS SQL]
    ↓
[3단계: JTS (Java)]
    ↓
[4단계: Spring + Hibernate Spatial]
    ↓
[5단계: 실습 프로젝트]
```

### 9.2 1단계: 기초 개념 (1-2일)

#### 필수 개념
| 개념 | 설명 | 학습 키워드 |
|------|------|------------|
| GIS | Geographic Information System (지리정보시스템) | GIS란? |
| 좌표계 | 위치를 숫자로 표현하는 체계 | WGS84, SRID, EPSG |
| 위도/경도 | 위도(latitude/Y), 경도(longitude/X) | lat/lng 순서 주의 |
| Geometry | 점, 선, 다각형 등 공간 데이터 타입 | Point, LineString, Polygon |
| Geography | 지구 곡면을 고려한 Geometry | 거리 계산 시 사용 |

#### 추천 자료
- **YouTube**: "GIS 입문" 검색 (한글)
- **블로그**: "PostGIS 시작하기" 검색
- **문서**: [PostGIS 공식 소개](https://postgis.net/workshops/postgis-intro/)

### 9.3 2단계: PostGIS SQL (3-5일)

#### 핵심 함수 (우선 학습)

```sql
-- 1. Point 생성
ST_MakePoint(longitude, latitude)
ST_SetSRID(point, 4326)

-- 2. 거리 계산
ST_Distance(geom1, geom2)        -- Geometry (평면)
ST_Distance(geog1, geog2)        -- Geography (곡면, 미터 단위)

-- 3. 반경 검색
ST_DWithin(geom1, geom2, distance)

-- 4. 포함 여부
ST_Contains(polygon, point)
ST_Within(point, polygon)

-- 5. 타입 변환
geometry::geography              -- Geometry → Geography
```

#### 실습 예제
```sql
-- 설치 확인
SELECT PostGIS_Version();

-- 두 지점 간 거리 (서울역 ↔ 강남역)
SELECT ST_Distance(
    ST_SetSRID(ST_MakePoint(126.9720, 37.5547), 4326)::geography,  -- 서울역
    ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326)::geography   -- 강남역
) / 1000 AS distance_km;
-- 결과: 약 7.8km

-- 강남역 기준 3km 반경 검색
SELECT * FROM stores
WHERE ST_DWithin(
    location,
    ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326)::geography,
    3000
);
```

#### 추천 자료
- **공식 문서**: [PostGIS Reference](https://postgis.net/docs/reference.html)
- **한글 튜토리얼**: "PostGIS 튜토리얼" 검색
- **실습 환경**: Docker로 PostGIS 설치 후 직접 쿼리 실행

### 9.4 3단계: JTS - Java Topology Suite (2-3일)

#### JTS란?
- Java에서 공간 데이터를 다루는 라이브러리
- PostGIS와 호환되는 Geometry 타입 제공
- Hibernate Spatial이 내부적으로 사용

#### 핵심 클래스

```java
// 1. Geometry 타입들
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.LineString;

// 2. Geometry 생성
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.Coordinate;

// 3. 사용 예제
GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

// Point 생성 (경도, 위도 순서!)
Point point = factory.createPoint(new Coordinate(127.0276, 37.4979));

// 좌표 추출
double lng = point.getX();  // 경도
double lat = point.getY();  // 위도
```

#### 추천 자료
- **공식 문서**: [JTS GitHub](https://github.com/locationtech/jts)
- **JavaDoc**: [JTS API Docs](https://locationtech.github.io/jts/javadoc/)

### 9.5 4단계: Spring + Hibernate Spatial (3-5일)

#### 필요 의존성 (이미 설정됨)
```gradle
implementation 'org.hibernate.orm:hibernate-spatial'
implementation 'org.locationtech.jts:jts-core'
```

#### application.yml 설정 (이미 설정됨)
```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.PostgisPG95Dialect
```

#### Entity에서 Point 사용
```java
@Entity
public class Store {
    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;
}
```

#### Repository에서 Native Query 사용
```java
@Query(value = """
    SELECT * FROM stores s
    WHERE ST_DWithin(
        s.location,
        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
        :radius
    )
    """, nativeQuery = true)
List<Store> findStoresWithinRadius(
    @Param("lng") double longitude,
    @Param("lat") double latitude,
    @Param("radius") double radiusMeters
);
```

#### 추천 자료
- **Hibernate Spatial 문서**: [Hibernate Spatial Guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#spatial)
- **블로그**: "Spring Boot PostGIS" 검색

### 9.6 5단계: 실습 프로젝트 아이디어

| 프로젝트 | 주요 기능 | 학습 포인트 |
|---------|----------|------------|
| 주변 카페 찾기 | 현재 위치 기준 반경 검색 | ST_DWithin, 거리 정렬 |
| 배달 가능 지역 | 다각형 내 포함 여부 | ST_Contains, Polygon |
| 최단 경로 | 두 지점 간 거리 | ST_Distance |
| 지역별 통계 | 행정구역별 집계 | ST_Within, GROUP BY |

### 9.7 추천 학습 순서 (총 2-3주)

| 주차 | 학습 내용 | 목표 |
|------|----------|------|
| **1주차** | 기초 개념 + PostGIS SQL | SQL로 공간 쿼리 작성 가능 |
| **2주차** | JTS + Hibernate Spatial | Java Entity에서 Point 사용 가능 |
| **3주차** | 실습 프로젝트 | 반경 검색 API 직접 구현 |

### 9.8 참고 링크 모음

#### 공식 문서
- [PostGIS 공식](https://postgis.net/documentation/)
- [JTS GitHub](https://github.com/locationtech/jts)
- [Hibernate Spatial](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#spatial)

#### 한글 자료
- 검색 키워드: "PostGIS 입문", "Spring Boot PostGIS", "JTS 사용법"
- Velog, Tistory 블로그에 좋은 한글 튜토리얼 다수

#### 영문 자료 (심화)
- [Introduction to PostGIS Workshop](https://postgis.net/workshops/postgis-intro/)
- [Baeldung - Hibernate Spatial](https://www.baeldung.com/hibernate-spatial)

---

## 10. 용어 정리

| 용어 | 설명 |
|------|------|
| **PostGIS** | PostgreSQL의 공간 데이터 확장 |
| **JTS** | Java Topology Suite, Java용 공간 라이브러리 |
| **Hibernate Spatial** | JPA에서 공간 타입 지원 |
| **SRID** | 좌표계 식별자 (4326 = WGS84) |
| **WGS84** | GPS 표준 좌표계 |
| **Geometry** | 평면 좌표계 기반 공간 타입 |
| **Geography** | 지구 곡면 기반 공간 타입 (거리 계산에 사용) |
| **GIST** | 공간 인덱스 타입 |
| **pg_trgm** | PostgreSQL 텍스트 유사도 검색 확장 |
| **GIN** | 텍스트 검색용 인덱스 타입 |