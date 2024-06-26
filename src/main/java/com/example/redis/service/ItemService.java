package com.example.redis.service;

import com.example.redis.SlowDataQuery;
import com.example.redis.dto.ItemDto;
import com.example.redis.entity.Item;
import com.example.redis.repo.ItemRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
  private final SlowDataQuery repository;
  private final ItemRepository itemRepository;

  //@Resource(name = "cacheRedisTemplate") = cacheRedisTemplate이라는 이름을 가진 빈을 주입받는다
  @Resource(name = "cacheRedisTemplate")
  //Redis에서 키-값 쌍에 대한 연산을 수행하기 위한 인터페이스입니다
  private ValueOperations<Long, ItemDto> cacheOps;

  //rankTemplate이라는 이름을 가진 빈을 주입받기.
  @Resource(name = "rankTemplate")
  //Redis의 정렬된 집합(ZSet)과 상호작용하기 위한 설정
  private ZSetOperations<String, ItemDto> rankOps;

  public ItemDto createManual(ItemDto dto) {
    Item item = itemRepository.save(Item.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .stock(dto.getStock())
            .build());
    ItemDto newDto = ItemDto.fromEntity(item);
    // 결과를 반환하기 전 캐시에 한번 저장한다.
    cacheOps.set(newDto.getId(), newDto, Duration.ofSeconds(60));
    return newDto;
  }

  // cacheName: 캐시 규칙을 지정하기 위한 이름
  // key: 캐시를 저장할때 개별 데이터를 구분하기 위한 값
  @CachePut(cacheNames = "itemCache", key = "#result.id")
  public ItemDto create(ItemDto dto) {
    // CachePut은 항상 메서드를 실행하고 해당 결과를 캐시에 적용한다.
    log.info("cacheput create");
    return ItemDto.fromEntity(itemRepository.save(Item.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .stock(dto.getStock())
            .build()));
  }

  @Cacheable(cacheNames = "itemAllCache", key = "#root.methodName")
  public List<ItemDto> readAll() {
    return repository.findAll()
            .stream()
            .map(ItemDto::fromEntity)
            .toList();
  }

  // cacheName: 캐시 규칙을 지정하기 위한 이름
  // key: 캐시를 저장할때 개별 데이터를 구분하기 위한 값
  @Cacheable(cacheNames = "itemCache", key = "#root.args[0]")
  public ItemDto readOne(Long id) {
    // 캐시에서 데이터를 찾으면 메서드 자체를 호출하지 않는다.
    log.info("cacheable readOne");
    return repository.findById(id)
            .map(ItemDto::fromEntity)
            .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public ItemDto readOneManual(Long id) {
    // Cache Aside를 RedisTemplate을 활용해 직접 구현해 보자.
    // 1. cacheOps에서 ItemDto를 찾아본다.
    // GET id
    ItemDto found = cacheOps.get(id);
    // 2. null일 경우 데이터베이스에서 조회한다.
    if (found == null) {
      // 2-1. 없으면 404
      found = repository.findById(id)
              .map(ItemDto::fromEntity)
              .orElseThrow(() ->
                      new ResponseStatusException(HttpStatus.NOT_FOUND));
      // 2-2. 있으면 캐시에 저장
      // 3번쩨 인자로 만료 시간 설정 가능
      cacheOps.set(id, found, Duration.ofSeconds(60));
    }
    // 3. 최종적으로 데이터를 반환한다.
    return found;
//        return repository.findById(id)
//                .map(ItemDto::fromEntity)
//                .orElseThrow(() ->
//                        new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public void purchase(Long id) {
    ItemDto item = ItemDto.fromEntity(repository.purchase(id));
    // soldRanks 1증가
    rankOps.incrementScore("soldRanks", item, 1);
  }

  public List<ItemDto> getMostSold() {
    // soldRanks에서 0부터 9번째 까지를 역순으로 가져와서 ranks에 저장
    Set<ItemDto> ranks = rankOps.reverseRange("soldRanks", 0, 9);
    // LinkedHashSet: 순서가 존재하는 집합
    if (ranks == null) return Collections.emptyList();

    log.info(String.valueOf(ranks.getClass()));
    return ranks.stream().toList();
//        return new ArrayList<>(ranks);
  }
}
