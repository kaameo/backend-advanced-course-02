# 회원·게시판·댓글 API

회원 가입·로그인 (JWT), 게시글 (검색 포함), 댓글과 한 단계 대댓글을 제공하는 게시판 REST API 서버

누구나 글과 댓글을 읽을 수 있고, 로그인한 사람만 쓸 수 있으며, 자기가 쓴 것만 고치고 지울 수 있다.

| 구분       | 사용 기술          |
|------------|--------------------|
| 언어       | Java 21            |
| 프레임워크 | Spring Boot 3.5.16 |
| 인증       | JWT (jjwt 0.12)    |
| DB         | PostgreSQL 17      |
| 실행       | Docker Compose     |

## 실행 방법

### 실행

```bash
cp .env.example .env
docker compose up --build
```

- 앱: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Open API: http://localhost:8080/v3/api-docs

| 초기 데이터 | 내용                                                                                                                       |
|-------------|----------------------------------------------------------------------------------------------------------------------------|
| 회원 3명    | `example1@example.com`(홍길동), `example2@example.com`(김철수), `example3@example.com`(이영희). 비밀번호는 모두 `12345678` |
| 게시글 12개 | 세개의 회원이 번갈아 작성                                                                                                  |
| 댓글 18개   | 짝수 번 글에만 3개씩 (댓글 0개인 글과 비교할 수 있도록)                                                                    |
| 대댓글 12개 | 짝수 번 글의 첫 댓글에 2개씩.                                                                                              |

### 종료 / 초기화

```bash
docker compose down      # 종료 (데이터 유지)
docker compose down -v   # 종료 + DB 볼륨 삭제 (예시 데이터부터 다시 생성)
```

### 테스트

```bash
./gradlew test
```

### 환경 변수

| 이름                                      | 기본값                                   | 설명                                                                                                             |
|-------------------------------------------|------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| `DATABASE_URL`                            | `jdbc:postgresql://localhost:5432/board` | Compose에서는 `jdbc:postgresql://postgres:5432/board`                                                            |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `board` / `board`                        |                                                                                                                  |
| `JWT_SECRET`                              | 없음 (`.env`에서 읽음)                   | JWT 서명 키. **32바이트 이상**이어야 한다. `.env.example`의 값은 로컬 실행용 예시이므로 운영에서는 반드시 바꾼다 |

## API 명세

### 공통

- 기본 주소: `http://localhost:8080/api/v1`
- 요청, 응답 본문: JSON (`Content-Type: application/json`)
- 인증이 필요한 요청: `Authorization: Bearer {accessToken}` 헤더
- 시각: `LocalDateTime` ISO-8601 문자열 (예: `2026-09-26T22:14:04.53424`)

### 엔드포인트 요약

| 메서드 | 주소                                           | 인증          | 성공 | 설명                                       |
|--------|------------------------------------------------|---------------|------|--------------------------------------------|
| POST   | `/auth/sign-up`                                | 불필요        | 201  | 회원 가입                                  |
| POST   | `/auth/login`                                  | 불필요        | 200  | 로그인 (토큰 발급)                         |
| GET    | `/members/me`                                  | 필요          | 200  | 내 정보                                    |
| POST   | `/posts`                                       | 필요          | 201  | 글 쓰기                                    |
| GET    | `/posts?keyword=&page=0&size=10`               | 불필요        | 200  | 글 목록 (최신순), 제목·본문 검색           |
| GET    | `/posts/{postId}`                              | 불필요        | 200  | 글 상세                                    |
| PUT    | `/posts/{postId}`                              | 필요 (작성자) | 200  | 글 수정                                    |
| DELETE | `/posts/{postId}`                              | 필요 (작성자) | 204  | 글 삭제 (댓글도 함께 삭제)                 |
| POST   | `/posts/{postId}/comments`                     | 필요          | 201  | 댓글 쓰기                                  |
| POST   | `/posts/{postId}/comments/{commentId}/replies` | 필요          | 201  | 대댓글 쓰기                                |
| GET    | `/posts/{postId}/comments`                     | 불필요        | 200  | 한 글의 댓글 목록 (대댓글 중첩, 오래된 순) |
| PUT    | `/posts/{postId}/comments/{commentId}`         | 필요 (작성자) | 200  | 댓글·대댓글 수정                           |
| DELETE | `/posts/{postId}/comments/{commentId}`         | 필요 (작성자) | 204  | 댓글·대댓글 삭제                           |

### 인증

#### `POST /auth/sign-up` 회원 가입

요청:

```json
{
  "email": "demo@example.com",
  "password": "password123",
  "nickname": "데모"
}
```

| 필드       | 규칙                                                          |
|------------|---------------------------------------------------------------|
| `email`    | 필수, 이메일 형식, 중복 불가                                  |
| `password` | 필수, 8~64자. BCrypt로 해시해 저장하고 응답에 내보내지 않는다 |
| `nickname` | 필수, 20자 이하                                               |

응답 `201 Created`

```json
{
  "id": 4,
  "nickname": "데모",
  "email": "demo@example.com",
  "createdAt": "2026-09-26T22:13:54.295058512",
  "modifiedAt": "2026-09-26T22:13:54.295058512"
}
```

| 상태 | 경우                                        |
|------|---------------------------------------------|
| 400  | 이메일 형식 오류, 비밀번호 길이 부족, 빈 값 |
| 409  | 이미 가입된 이메일                          |

#### `POST /auth/login` 로그인

요청:

```json
{
  "email": "demo@example.com",
  "password": "password123"
}
```

응답 `200 OK`:

```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer"
}
```

| 상태 | 경우                                                 |
|------|------------------------------------------------------|
| 400  | 이메일 형식 오류, 빈 값                              |
| 401  | 없는 이메일 또는 틀린 비밀번호 (두 경우 같은 메시지) |

### 회원

#### `GET /members/me` 내 정보

응답 `200 OK`: 가입 응답과 같은 모양 (`id`, `nickname`, `email`, `createdAt`, `modifiedAt`)

| 상태 | 경우                        |
|------|-----------------------------|
| 401  | 토큰이 없거나 잘못됨·만료됨 |

### 게시글

#### `POST /posts` 글 쓰기

요청:

```json
{
  "title": "첫 글",
  "content": "안녕하세요"
}
```

| 필드      | 규칙             |
|-----------|------------------|
| `title`   | 필수, 100자 이하 |
| `content` | 필수             |

응답 `201 Created`

```json
{
  "id": 13,
  "title": "첫 글",
  "content": "안녕하세요",
  "authorId": 4,
  "authorNickname": "데모",
  "createdAt": "2026-09-26T22:14:04.534240419",
  "modifiedAt": "2026-09-26T22:14:04.534240419"
}
```

| 상태 | 경우                                   |
|------|----------------------------------------|
| 400  | 제목·본문이 비었거나 제목이 100자 초과 |
| 401  | 로그인하지 않음                        |

#### `GET /posts` 글 목록·검색

| 쿼리 파라미터 | 기본값 | 설명                                                                                                                  |
|---------------|--------|-----------------------------------------------------------------------------------------------------------------------|
| `keyword`     | 없음   | 제목 **또는** 본문에 포함된 글만 찾는다. 대소문자를 구분하지 않고, 앞뒤 공백은 무시한다. 없거나 비어 있으면 전체 목록 |
| `page`        | 0      | 0부터 시작하는 페이지 번호                                                                                            |
| `size`        | 10     | 한 페이지의 글 수                                                                                                     |

정렬은 최신순 (`createDate` 내림차순)으로 고정이다. 검색 결과가 없으면 404가 아닌 빈 `content`와 `totalElements: 0`을 리턴한다.

응답 `200 OK`:

```json
{
  "content": [
    {
      "id": 13,
      "title": "첫 글",
      "authorNickname": "데모",
      "commentCount": 2,
      "createdAt": "2026-09-26T22:49:45.031589",
      "modifiedAt": "2026-09-26T22:49:45.031589"
    }
  ],
  "page": {
    "size": 3,
    "number": 0,
    "totalElements": 13,
    "totalPages": 5
  }
}
```

목록에는 본문 대신 작성자 닉네임과 댓글 수를 담는다. `commentCount`는 대댓글을 포함한 수다.

#### `GET /posts/{postId}` 글 상세

응답 `200 OK`: POST 응답과 같은 모양

| 상태 | 경우    |
|------|---------|
| 404  | 없는 글 |

#### `PUT /posts/{postId}` 글 수정

요청·응답은 글 쓰기와 같은 모양이다.

| 상태 | 경우            |
|------|-----------------|
| 400  | 입력 오류       |
| 401  | 로그인하지 않음 |
| 403  | 본인 글이 아님  |
| 404  | 없는 글         |

#### `DELETE /posts/{postId}` 글 삭제

응답 `204 No Content`. 글에 달린 댓글도 함께 삭제한다.

| 상태 | 경우            |
|------|-----------------|
| 401  | 로그인하지 않음 |
| 403  | 본인 글이 아님  |
| 404  | 없는 글         |

### 댓글

#### `POST /posts/{postId}/comments` 댓글 쓰기

요청:

```json
{
  "content": "첫 댓글"
}
```

응답 `201 Created`

```json
{
  "id": 31,
  "postId": 13,
  "parentId": null,
  "content": "첫 댓글",
  "authorId": 4,
  "authorNickname": "데모",
  "createdAt": "2026-09-26T22:49:45.055111757",
  "modifiedAt": "2026-09-26T22:49:45.055111757",
  "replies": []
}
```

- `parentId`: 대댓글이면 부모 댓글 id, 최상위 댓글이면 `null`
- `replies`: 목록 조회에서만 채워진다. 작성, 수정 응답에서는 빈 배열

| 상태 | 경우            |
|------|-----------------|
| 400  | 본문이 비었음   |
| 401  | 로그인하지 않음 |
| 404  | 없는 글         |

#### `POST /posts/{postId}/comments/{commentId}/replies` 대댓글 쓰기

`commentId` 댓글에 답글을 단다. 요청은 댓글 쓰기와 같다.

```json
{
  "content": "첫 대댓글"
}
```

응답 `201 Created`

```json
{
  "id": 32,
  "postId": 13,
  "parentId": 31,
  "content": "첫 대댓글",
  "authorId": 4,
  "authorNickname": "데모",
  "createdAt": "2026-09-26T22:49:45.078854007",
  "modifiedAt": "2026-09-26T22:49:45.078854007",
  "replies": []
}
```

| 상태 | 경우                                                 |
|------|------------------------------------------------------|
| 400  | 본문이 비었음, 또는 대댓글에 다시 답글을 달려고 함   |
| 401  | 로그인하지 않음                                      |
| 404  | 없는 글·댓글, 또는 댓글이 주소의 글에 달린 것이 아님 |

#### `GET /posts/{postId}/comments` 댓글 목록

응답 `200 OK`

```json
[
  {
    "id": 31,
    "postId": 13,
    "parentId": null,
    "content": "첫 댓글",
    "authorNickname": "데모",
    ...,
    "replies": [
      {
        "id": 32,
        "postId": 13,
        "parentId": 31,
        "content": "첫 대댓글",
        "authorNickname": "데모",
        ...,
        "replies": []
      }
    ]
  }
]
```

| 상태 | 경우                           |
|------|--------------------------------|
| 404  | 없는 글 (빈 배열이 아니라 404) |

#### `PUT /posts/{postId}/comments/{commentId}` 댓글 수정

요청·응답은 댓글 쓰기와 같은 모양이다. 대댓글도 이 주소로 수정한다.

| 상태 | 경우                                              |
|------|---------------------------------------------------|
| 400  | 본문이 비었음                                     |
| 401  | 로그인하지 않음                                   |
| 403  | 본인 댓글이 아님                                  |
| 404  | 없는 댓글, 또는 댓글이 주소의 글에 달린 것이 아님 |

#### `DELETE /posts/{postId}/comments/{commentId}` 댓글 삭제

응답 `204 No Content`. 최상위 댓글을 지우면 그 아래 대댓글도 함께 삭제한다.

### 오류 응답

모든 오류는 같은 모양으로 응답한다.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "게시글이 없습니다. id=999"
}
```

| 상태 | 경우                                               | `message` 예                                                                             |
|------|----------------------------------------------------|------------------------------------------------------------------------------------------|
| 400  | 입력 검증 실패                                     | `password: 크기가 8에서 64 사이여야 합니다, email: 올바른 형식의 이메일 주소여야 합니다` |
| 400  | JSON 형식 오류                                     | `요청 본문 형식이 올바르지 않습니다.`                                                    |
| 400  | 경로 변수 타입 오류 (`/posts/abc`)                 | `id: 값의 형식이 올바르지 않습니다.`                                                     |
| 400  | 대댓글에 답글                                      | `대댓글에는 답글을 달 수 없습니다.`                                                      |
| 401  | 로그인이 필요한 요청에 토큰이 없거나 잘못됨·만료됨 | `로그인이 필요합니다.`                                                                   |
| 401  | 로그인 실패                                        | `이메일 또는 비밀번호가 올바르지 않습니다.`                                              |
| 403  | 남의 글·댓글 수정·삭제                             | `본인이 작성한 글만 수정 및 삭제할 수 있습니다.`                                         |
| 404  | 없는 글·댓글·회원, 없는 주소                       | `게시글이 없습니다. id=999`                                                              |
| 405  | 지원하지 않는 메서드                               | `지원하지 않는 메서드입니다: POST`                                                       |
| 415  | 요청 본문이 JSON이 아님 (`Content-Type` 누락 등)   | `요청 본문은 application/json이어야 합니다.`                                             |
| 409  | 이메일 중복                                        | `이미 사용 중인 이메일입니다.`                                                           |
| 500  | 예상하지 못한 오류                                 | `서버 내부 오류가 발생했습니다.` (원인은 서버 로그에만 남김)                             |

## 설계 설명

### JWT 로그인 방식

로그인하면 서버가 회원 id (`sub`)와 발급, 만료 시각을 담은 토큰을 HMAC으로 서명한다.
클라이언트는 이후 요청마다 `Authorization: Bearer` 헤더로 보내고, 서명과 만료를 확인해 회원 id를 인증 정보로 등록한다.
컨트롤러는 `@AuthenticationPrincipal Integer memberId`로 받는다.

JWT를 고른 이유:

- 서버에 로그인 상태를 저장하지 않는다.
- 세션 저장소가 필요 없이 서명만 검증하면 된다.
- 서버를 여러 대로 늘려도 세션을 공유할 필요가 없다.

감수한 점:

- 만료 전에는 토큰을 무효화할 수 없다.
    - 로그아웃해도 토큰이 유효하다.
- 토큰 본문은 인코딩이다.
    - 누구나 디코딩해 읽을 수 있어 회원 id 외의 정보 (이메일, 닉네임)는 넣지 않는다.

### API 설계

| 결정                                                                | 이유                                                                                                    |
|---------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `/api/v1` 접두사                                                    | 호환되지 않는 변경이 생기면 `/api/v2`로 나눌 수 있다                                                    |
| 리소스는 복수 명사 (`/posts`, `/comments`), 동작은 HTTP 메서드      | REST 관례                                                                                               |
| 가입과 로그인은 `/auth` 아래                                        | `/api/v1/auth/**` 한곳에 모여 보안 설정이 단순하고 실수가 줄어든다                                      |
| 댓글은 `/posts/{postId}/comments`                                   | 댓글은 글에 속한 리소스다이고, 주소의 글과 댓글이 맞지 않는 것(다른 글의 댓글 번호)을 방지한다          |
| 대댓글은 `/comments/{commentId}/replies` (본문의 `parentId`가 아님) | 부모 댓글이 주소에 드러나고, 기존 "글에 달린 댓글인지" 을 그대로 쓴다.                                  |
| 수정은 `PUT`                                                        | 제목과 본문을 모두 보내 통째로 바꾼다                                                                   |
| 생성은 201 + `Location`, 삭제는 204                                 | 새 리소스의 주소를 헤더로 알려 주고, 삭제 후에는 돌려줄 본문이 없다                                     |
| 로그인은 200                                                        | 리소스 저장이 아닌 토큰을 발급하는 동작이다                                                             |
| 이메일 중복은 409                                                   | 입력 형식은 맞지만 현재 상태(이미 있는 이메일)와 충돌한다                                               |
| 로그인 실패는 이유와 관계없이 같은 401 메시지                       | "없는 이메일"과 "틀린 비밀번호"를 구분하면 가입된 이메일을 알아낼 수 있다                               |
| 404를 403보다 먼저 확인                                             | 글이 없으면 404, 있는데 남의 글이면 403 반환                                                            |
| 목록 정렬은 서버가 고정                                             | 최신순이 요구사항이다                                                                                   |
| 검색은 `/posts/search`가 아니라 `/posts?keyword=`                   | 검색 결과도 "글 목록"을 조건으로 거른 것이라 쿼리 파라미터가 REST 에 맞다                               |
| 페이지 응답은 `content` + `page`                                    | Spring Data `Page`를 그대로 직렬화하면 버전에 따라 JSON 모양이 바뀔 수 있어 `VIA_DTO` 방식으로 고정했다 |
| 회원 조회는 `/members/me`만 제공                                    | 이메일은 로그인 아이디라 다른 회원에게 공개하지 않는다                                                  |
| 요청·응답은 모두 DTO(record)                                        | 엔티티를 그대로 내보내면 비밀번호가 노출되고, 양방향 연관관계 직렬화나 지연 로딩 예외가 생긴다          |

### 엔티티 관계

```
Member      1 ──< N Post          (Post.author, 작성자)
Member      1 ──< N PostComment   (PostComment.author, 작성자)
Post        1 ──< N PostComment   (PostComment.post, 달린 글)
PostComment 1 ──< N PostComment   (PostComment.parentComment, 부모 댓글. 최상위 댓글은 null)
```

| 엔티티        | 필드                                         | 제약                                                                  |
|---------------|----------------------------------------------|-----------------------------------------------------------------------|
| `Member`      | `email`, `password`, `nickname`              | `email` unique. `password`는 BCrypt 해시                              |
| `Post`        | `title`, `content`, `author`                 | `title` 100자, `content` TEXT, `author` 필수                          |
| `PostComment` | `content`, `post`, `author`, `parentComment` | `content` TEXT, `post`·`author` 필수, `parentComment`는 대댓글일 때만 |

- 세 엔티티 모두 `BaseIdAndTime`을 상속해 `id`, `createDate`, `modifyDate`를 갖는다. JPA Auditing을 통해 시각을 채운다.
- 모든 `@ManyToOne`은 지연 로딩 (`LAZY`)이다. `parentComment`를 뺀 나머지는 `optional = false`라 작성자 없는 글, 글 없는 댓글은 만들 수 없다.
- `Post.comments`(`@OneToMany(mappedBy = "post")`)는 목록 쿼리에서 댓글 수를 셀 때만 쓴다.
- 엔티티에는 setter를 두지 않고, 생성자와 `update()` 메서드로만 값을 바꾼다. 트랜잭션 안에서 필드를 바꾸면 변경 감지로 수정이 반영된다.

### 게시글 검색

글 목록 쿼리 하나에 검색 조건을 넣었다. 조건은 `group by` 앞에 두어 글을 먼저 거른 뒤 댓글 수를 센다.
전체 글 수를 세는 `countQuery`에도 같은 조건을 넣어야 `totalElements`가 검색 결과 수로 나온다.

```java
where lower(p.title) like lower(

concat('%',:keyword, '%'))

or lower(p.content) like lower(

concat('%',:keyword, '%'))
```

- keyword를 빈문자열로 넘겨 쿼리 하나로 전체 목록과 검색을 함께 처리한다.
- 대소문자 구분 없이 양쪽을 `lower()`로 바꿔 비교한다.

한계:

| 한계                             | 설명                                                                                                    | 개선 방법                                           |
|----------------------------------|---------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| 인덱스를 쓰지 못함               | 앞에 `%`가 붙은 `LIKE '%키워드%'`는 B-tree 인덱스를 쓸 수 없어, 글이 많아지면 매번 테이블 전체를 읽는다 | Elasticsearch같은 전문 검색                         |
| `%`, `_`를 문자 그대로 찾지 못함 | 사용자가 입력한 `%`, `_`가 LIKE 와일드카드로 해석된다 (`keyword=%`는 전체 글과 일치)                    | 키워드의 `%`, `_`를 이스케이프하고 `escape` 절 사용 |

### 대댓글

댓글 → 대댓글 한 단계만 허용한다. null이 아니면 대댓글이다.

```java
public static PostComment reply(PostComment parent, Member author, String content) {
    PostComment reply = new PostComment(parent.getPost(), author, content);
    reply.parentComment = parent;
    return reply;
}
```

- 깊이 제한: 부모가 이미 대댓글이면 `BadRequestException`(400)으로 막는다.
- 대댓글의 글은 부모의 글로 정한다: 대댓글이 부모와 다른 글에 달리는 일을 생성 단계에서 방지한다.
- 목록은 쿼리 한 번: 글의 모든 댓글 (최상위 + 대댓글)을 작성자와 함께 fetch join으로 한 번에 가져온 뒤 부모 id별로 묶어 `replies`에 넣는다.
    - 최상위 댓글마다 대댓글을 따로 조회하면 N+1이 생기기 때문이다.

```java
Map<Integer, List<PostCommentResponseDto>> repliesByParentId = comments.stream()
        .filter(PostComment::isReply)
        .collect(Collectors.groupingBy(
                c -> c.getParentComment().getId(),
                Collectors.mapping(PostCommentResponseDto::from, Collectors.toList())
        ));
```

- 부모 댓글을 지우면 대댓글도 함께 지운다
    - 대댓글을 벌크 삭제한 뒤 부모를 지운다.
- `deleted`로 표시만 하고 대댓글을 남기는 방식 (soft delete)은 요구사항에 없어 고르지 않았다.

### 글 삭제 시 댓글 처리

글을 지우면 그 글의 댓글도 함께 삭제한다. 댓글은 글에 속한 데이터라 글 없이 댓글이 남아 있을 이유가 없다.

```java

@Transactional
public void delete(int memberId, int postId) {
    Post post = getPost(postId);                        // 없으면 404
    checkAuthor(post, memberId);                        // 본인 글이 아니면 403
    postCommentRepository.deleteAllByPostId(postId);   // 댓글 벌크 삭제
    postRepository.delete(post);                        // 글 삭제
}
```

- 404, 403을 통과한 뒤에만 댓글을 지운다.
- 댓글 먼저 지우고, 그 뒤에 글을 지운다.
- 대댓글도 댓글처럼 한 번에 지운다.
- 한 트랜잭션으로 묶어 글 삭제가 실패하면 댓글 삭제도 롤백된다.

검토한 다른 방식:

| 방식                    | 고르지 않은 이유                                                                    |
|-------------------------|-------------------------------------------------------------------------------------|
| `cascade = REMOVE`      | 댓글 수만큼 DELETE 쿼리가 나간다                                                    |
| DB `ON DELETE CASCADE`  | 쿼리는 가장 적지만, 삭제 규칙이 코드가 아닌 DB에 있다.                              |
| 삭제 표시 (soft delete) | 복구가 필요하다는 요구사항이 없고, 모든 조회에 `deleted = false` 조건을 붙여야 한다 |

### N+1 문제

#### 원인

`Post.author`, `Post.comments`, `PostComment.author`는 모두 지연 로딩 (`LAZY`)이다.
엔티티 목록을 조회한 뒤 DTO로 바꾸면서 연관 데이터를 꺼내면, 꺼낼 때마다 SELECT가 따로 나간다.

- 작성자 닉네임 (`getAuthor().getNickname()`): 서로 다른 작성자 수만큼 `member` 조회
- 댓글 수 (`getComments().size()`): 글 수만큼 `post_comment` 조회. 개수만 필요한데 댓글 내용까지 모두 가져온다

결과 행 수 (N)에 비례해 쿼리가 늘어난다.
`hibernate.generate_statistics`를 true로 바꾸고 요청당 실행된 JDBC 문 개수로 확인 가능하다.

```yaml
spring:
  jpa:
    properties:
      hibernate:
        # default_batch_fetch_size: 100   # N+1 완화
        generate_statistics: true
```

#### 개선 전 측정

**댓글 목록** `GET /api/v1/posts/4/comments` (댓글 3개, 작성자 3명): **쿼리 5번**

| #   | 쿼리                                                         | 원인                                                     |
|-----|--------------------------------------------------------------|----------------------------------------------------------|
| 1   | `select ... from post where id=?`                            | 글 존재 확인 (없으면 404)                                |
| 2   | `select ... from post_comment join post ... where p1_0.id=?` | 댓글 목록. 메서드 이름 쿼리가 불필요하게 `post`를 join함 |
| 3~5 | `select ... from member where id=?` × 3                      | 댓글마다 작성자를 따로 조회 (N+1)                        |

```
Session Metrics {
    ...
    2675250 nanoseconds spent executing 5 JDBC statements;
    ...
}
```

**글 목록** `GET /api/v1/posts` (작성자 3명): 쿼리가 `글 목록 1 + 전체 개수 1 + 작성자 수 + 글 수`만큼 나간다.

| #   | 쿼리                                                                                   | 원인                                                                               |
|-----|----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| 1   | `select ... from post order by create_date desc offset ? rows fetch first ? rows only` | 글 목록 (페이지)                                                                   |
| 2   | `select count(p1_0.id) from post`                                                      | 전체 글 수 (페이지 정보)                                                           |
| 3~5 | `select ... from member where id=?` × 3                                                | 글마다 작성자를 따로 조회. 같은 회원은 영속성 컨텍스트에 남아 작성자 수만큼만 나감 |
| 6~  | `select ... from post_comment where post_id=?` × 글 수                                 | 댓글 수를 세려고 글마다 댓글 전체를 조회                                           |

| 요청              | 글 수 | 실행된 JDBC 문          |
|-------------------|-------|-------------------------|
| `?page=0&size=10` | 10    | **15** (1 + 1 + 3 + 10) |
| `?page=0&size=12` | 12    | **17** (1 + 1 + 3 + 12) |

글이 2개 늘자 쿼리도 2개 늘었다. 한 페이지의 글 수에 비례해 쿼리가 늘어나는 N+1 구조다.

#### 개선 방법과 결과

**댓글 목록: fetch join** (5 → **2**)

댓글과 작성자를 fetch join으로 한 번에 가져온다.

```java

@Query("""
        select c from PostComment c
        join fetch c.author
        where c.post.id = :postId
        order by c.createDate asc
        """)
List<PostComment> findAllWithAuthorByPostId(@Param("postId") int postId);
```

| # | 쿼리                                                                                                                              |
|---|-----------------------------------------------------------------------------------------------------------------------------------|
| 1 | `select ... from post where id=?` (글 존재 확인)                                                                                  |
| 2 | `select ... from post_comment pc1_0 join member a1_0 on a1_0.id=pc1_0.author_id where pc1_0.post_id=? order by pc1_0.create_date` |

```
Session Metrics {
    ...
    1826209 nanoseconds spent executing 2 JDBC statements;
    ...
}
```

작성자 조회가 댓글 쿼리에 합쳐졌고, 불필요한 `post` join도 사라졌다. 댓글과 작성자가 몇 명이든 쿼리는 2번이다.

**글 목록: DTO 조회 + 집계 쿼리** (15·17 → **2**)

엔티티를 가져와 DTO로 바꾸는 대신, 목록에 필요한 값만 쿼리 한 번으로 DTO에 담는다. 댓글 수는 댓글을 불러와 세지 않고 DB가 `count()`로 계산한다.

```java

@Query(value = """
        select new com.board.post.dto.PostListItemDto(
            p.id, p.title, m.nickname, count(c), p.createDate, p.modifyDate
        )
        from Post p
        join p.author m
        left join p.comments c
        group by p.id, p.title, m.nickname, p.createDate, p.modifyDate
        order by p.createDate desc
        """,
        countQuery = "select count(p) from Post p")
Page<PostListItemDto> findPostList(Pageable pageable);
```

- `join p.author`: 작성자 닉네임을 같은 쿼리에서 가져온다
- `left join p.comments`: 댓글이 없는 글도 목록에 남긴다 (`count`가 0)
- `countQuery`: 전체 글 수를 셀 때는 join이 필요 없어 따로 가볍게 센다

댓글 컬렉션 (`@OneToMany`)을 fetch join하지 않은 이유: 컬렉션을 fetch join하면 글 1개가 댓글 수만큼 여러 행으로 늘어나, DB에서 페이지를 자를 수 없다.
Hibernate는 이때 전체 결과를 메모리에 올린 뒤 페이징한다.

| # | 쿼리                                                                                                                                                                                                     |
|---|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | `select p1_0.id, p1_0.title, a1_0.nickname, count(c1_0.id), ... from post p1_0 join member a1_0 ... left join post_comment c1_0 ... group by ... order by p1_0.create_date desc fetch first ? rows only` |
| 2 | `select count(p1_0.id) from post p1_0`                                                                                                                                                                   |

| 요청              | 글 수 | 개선 전 | 개선 후 |
|-------------------|-------|---------|---------|
| `?page=0&size=10` | 10    | 15      | **2**   |
| `?page=0&size=12` | 12    | 17      | **2**   |

글 수와 관계없이 쿼리는 2번이다. 목록에 필요 없는 글 본문 (`content`, `TEXT`)도 더 이상 읽지 않는다.

글 삭제: 댓글 벌크 삭제 (6 → **3**, 댓글 3개 기준)

조회 쪽 N+1과 같은 구조의 문제가 삭제에도 있다. `cascade = CascadeType.REMOVE`는 삭제할 댓글을 먼저 조회한 뒤 댓글을 하나씩 지운다.

개선 전 (댓글 3개인 글 삭제): **6번**

```
select ... from post where id=?
select ... from post_comment where post_id=?     ← cascade 대상 조회
delete from post_comment where id=?              ← 댓글 수만큼 반복
delete from post_comment where id=?
delete from post_comment where id=?
delete from post where id=?
```

개선 후: **3번**. `cascade`를 없애고, 댓글을 JPQL 벌크 DELETE 한 번으로 지운 뒤 글을 지운다.

```java

@Modifying
@Query("delete from PostComment c where c.post.id = :postId")
void deleteAllByPostId(@Param("postId") int postId);
```

```
select ... from post where id=?
delete from post_comment pc1_0 where pc1_0.post_id=?    ← 한 번에
delete from post where id=?
```

댓글 수와 관계없이 쿼리는 3번이다.

## 실행 결과

응답 헤더는 상태 줄과 주요 헤더만 남겼다.

### 1. 가입

```
$ curl -i -X POST http://localhost:8080/api/v1/auth/sign-up \
    -H 'Content-Type: application/json' \
    -d '{"email":"demo@example.com","password":"password123","nickname":"데모"}'

HTTP/1.1 201
Location: /api/v1/members/me
Content-Type: application/json

{"id":4,"nickname":"데모","email":"demo@example.com","createdAt":"2026-09-26T22:49:44.82147509","modifiedAt":"2026-09-26T22:49:44.82147509"}
```

### 2. 로그인

```
$ curl -i -X POST http://localhost:8080/api/v1/auth/login \
    -H 'Content-Type: application/json' \
    -d '{"email":"demo@example.com","password":"password123"}'

HTTP/1.1 200
Content-Type: application/json

{"accessToken":"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI0IiwiaWF0IjoxNzkwNDMwNTg0LCJleHAiOjE3OTA0MzQxODR9.md0XSjq-7umQ3x-UD75dthkvD2sQB6ByxSBNImvbwTHfNFMj9JqYs5h4zYGMXmPv","tokenType":"Bearer"}
```

이후 요청의 `$TOKEN`은 이 `accessToken` 값이다.

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"password123"}' | jq -r .accessToken)
```

### 3. 글 쓰기

```
$ curl -i -X POST http://localhost:8080/api/v1/posts \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"title":"첫 글","content":"안녕하세요"}'

HTTP/1.1 201
Location: /api/v1/posts/13
Content-Type: application/json

{"id":13,"title":"첫 글","content":"안녕하세요","authorId":4,"authorNickname":"데모","createdAt":"2026-09-26T22:49:45.031589382","modifiedAt":"2026-09-26T22:49:45.031589382"}
```

### 4. 댓글 쓰기

```
$ curl -i -X POST http://localhost:8080/api/v1/posts/13/comments \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"content":"첫 댓글"}'

HTTP/1.1 201
Location: /api/v1/posts/13/comments/31
Content-Type: application/json

{"id":31,"postId":13,"parentId":null,"content":"첫 댓글","authorId":4,"authorNickname":"데모","createdAt":"2026-09-26T22:49:45.055111757","modifiedAt":"2026-09-26T22:49:45.055111757","replies":[]}
```

대댓글 쓰기:

```
$ curl -i -X POST http://localhost:8080/api/v1/posts/13/comments/31/replies \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"content":"첫 대댓글"}'

HTTP/1.1 201
Location: /api/v1/posts/13/comments/32
Content-Type: application/json

{"id":32,"postId":13,"parentId":31,"content":"첫 대댓글","authorId":4,"authorNickname":"데모","createdAt":"2026-09-26T22:49:45.078854007","modifiedAt":"2026-09-26T22:49:45.078854007","replies":[]}
```

### 5. 목록 조회

글 목록 (로그인 없이). 13번 글의 `commentCount`는 댓글 1 + 대댓글 1이다.

```
$ curl -i 'http://localhost:8080/api/v1/posts?page=0&size=3'

HTTP/1.1 200
Content-Type: application/json

{"content":[{"id":13,"title":"첫 글","authorNickname":"데모","commentCount":2,"createdAt":"2026-09-26T22:49:45.031589","modifiedAt":"2026-09-26T22:49:45.031589"},{"id":12,"title":"테스트 글 12","authorNickname":"홍길동","commentCount":5,"createdAt":"2026-09-26T22:49:32.297316","modifiedAt":"2026-09-26T22:49:32.297316"},{"id":11,"title":"테스트 글 11","authorNickname":"이영희","commentCount":0,"createdAt":"2026-09-26T22:49:32.296566","modifiedAt":"2026-09-26T22:49:32.296566"}],"page":{"size":3,"number":0,"totalElements":13,"totalPages":5}}
```

댓글 목록 (로그인 없이). 대댓글은 부모 댓글의 `replies` 안에 들어간다.

```
$ curl -i http://localhost:8080/api/v1/posts/13/comments

HTTP/1.1 200
Content-Type: application/json

[{"id":31,"postId":13,"parentId":null,"content":"첫 댓글","authorId":4,"authorNickname":"데모","createdAt":"2026-09-26T22:49:45.055112","modifiedAt":"2026-09-26T22:49:45.055112","replies":[{"id":32,"postId":13,"parentId":31,"content":"첫 대댓글","authorId":4,"authorNickname":"데모","createdAt":"2026-09-26T22:49:45.078854","modifiedAt":"2026-09-26T22:49:45.078854","replies":[]}]}]
```

검색 (로그인 없이). 한글 키워드는 `--data-urlencode`로 인코딩한다.

```
$ curl -i -G http://localhost:8080/api/v1/posts --data-urlencode 'keyword=첫'

HTTP/1.1 200
Content-Type: application/json

{"content":[{"id":13,"title":"첫 글","authorNickname":"데모","commentCount":2,"createdAt":"2026-09-26T22:49:45.031589","modifiedAt":"2026-09-26T22:49:45.031589"}],"page":{"size":10,"number":0,"totalElements":1,"totalPages":1}}
```

### 6. 401: 로그인 없이 글 쓰기

```
$ curl -i -X POST http://localhost:8080/api/v1/posts \
    -H 'Content-Type: application/json' \
    -d '{"title":"로그인 없이","content":"작성 시도"}'

HTTP/1.1 401
Content-Type: application/json;charset=UTF-8

{"status":401,"error":"Unauthorized","message":"로그인이 필요합니다."}
```

### 7. 403: 남의 글 수정

`demo@example.com`으로 로그인한 토큰으로, 홍길동이 쓴 12번 글을 수정한다.

```
$ curl -i -X PUT http://localhost:8080/api/v1/posts/12 \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"title":"남의 글","content":"수정 시도"}'

HTTP/1.1 403
Content-Type: application/json

{"status":403,"error":"Forbidden","message":"본인이 작성한 글만 수정 및 삭제할 수 있습니다."}
```

### 8. 404: 없는 글 조회

```
$ curl -i http://localhost:8080/api/v1/posts/999

HTTP/1.1 404
Content-Type: application/json

{"status":404,"error":"Not Found","message":"게시글이 없습니다. id=999"}
```
