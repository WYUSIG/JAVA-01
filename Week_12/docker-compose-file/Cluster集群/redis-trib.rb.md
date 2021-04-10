如果没有拉取镜像

```
docker pull zvelo/redis-trib
```

```
docker run --rm -it zvelo/redis-trib create --replicas 1 192.168.132.1:6379 192.168.132.1:6380 192.168.132.1:6381 192.168.132.1:6382 192.168.132.1:6383 192.168.132.1:6384
```

