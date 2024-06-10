## 📝Redis (Remote dictionary server)
Key - Value (dictionary)의 형태로 데이터를 저장할 수 있게 해주는 In-Memory Database이다. <br>
- In-Memory
  - 컴퓨터의 주기억장치인 RAM에 데이터를 올려서 사용하는 방법
<hr>

#### Redis를 사용해야 하는 이유
DB는 데이터를 디스크에 직접 저장하기 때문에 서버에 문제가 발생하더라도 데이터가 손실되지 않지만, 
매번 디스크에 접근하여 사용하기 때문에 사용자가 많아질수록 부하가 많아져 느려질 수 있다. 때문에 캐시 서버를 도입하여 사용해야 한다. <br>
즉, 서비스가 커질수록 Redis의 도입을 고려해 볼 만하다
- DB, Cache, MessageBroker로 사용 된다
<hr>

#### Cache
한 번 조회된 데이터를 미리 특정 공간에 저장해놓고, 요청이 다시 발생하게 되면 저장해놓은 데이터를 제공해서 빠르게 서비스를 제공해주는 것을 의미한다
- 캐시 서버는 Look aside cache 패턴과 Write Back 패턴이 존재한다.
<hr>

#### Look aside cache
1. 클라이언트가 데이터를 요청하면
2. 웹서버는 데이터가 존재하는지 Cache 서버에 먼저 확인한다
3. Cache 서버에 데이터가 있으면 DB에 데이터를 조회하지 않고, Cache 서버에 있는 결과값을 클라이언트에게 바로 반환한다 (Cache Hit)
4. Cache 서버에 데이터가 없으면 DB에 데이터를 조회하여 Cache 서버에 저장하고, 결과값을 클라이언트에게 반환한다 (Cache Miss)

#### Write Back
1. 웹서버는 모든 데이터를 Cache 서버에 저장한다
2. Cache 서버에 특정 시간 동안 데이터가 저장되면, Cache 서버에 있는 데이터를 한 번에 DB에 저장한다
3. DB에 저장 했으니, 기존에 DB에 저장된 Cache 서버의 잔존 데이터를 삭제한다.
- 즉 ,insert를 1개씩 500번 수행하는 것보다 500개를 한 번에 삽입하는 동작이 훨씬 빠르기 때문에 write back 방식은 빠른 속도로 서비스가 가능하다.
- 단점 - DB에 데이터를 저장하기 전에 캐시 서버가 죽으면 데이터가 유실된다는 문제점이 있다.
 
#### Redis 사용시 주의할 점
> Single Threaded
>> 한 번에 하나의 명령만 처리할 수 있다. 그렇기 때문에 중간에 처리 시간이 긴 명령어가 들어오면 그 뒤에 명령어들은 모두 앞에 있는 명령어가 처리될 때까지 대기해야 한다.
- 시간 복잡도
  - Single Threaded 사용으로 한 번에 하나의 명령만 수행이 가능하므로 처리 시간이 긴 요청의 경우 장애가 발생한다.
- 메모리 단편화
  - 크고 작은 데이터를 할당하고 해제하는 과정에서 메모리의 공간이 작은 조각으로 나누어져 사용 가능한 메모리가 충분히 존재하지만 할당(사용)이 불가능한 상태이다
  - 이 현상이 계속되면 프로세스가 죽는 현상이 발생 할 수도 있으므로, Redis사용 시 메모리를 적당히 여유있게 사용하는 것이 좋다
- 인메모리 데이터 저장소의 특성상, 서버에 장애가 발생했을 경우 `데이터 유실`이 발생할 수 있다.
<hr>

## Redis 조금만 맛보기 👀






 