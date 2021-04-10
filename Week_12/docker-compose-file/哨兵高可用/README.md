运行这个yml文件之前，需要先运行主从复制的yml

然后查看master 容器id

docker ps

然后执行下方命令查看master容器详细信息

docker inspect  master容器id

然后你会在最后看到这么一段信息

```
"Networks": {
                "_default": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": [
                        "ad6c9444717b",
                        "master"
                    ],
                    "NetworkID": "a15fac001f1c4a7c0ed1d8fd4df5eb69f9b1ce59e729f4d629ff7bf18bb36e00",
                    "EndpointID": "8e20276491e81352278ae38ac697ff772ca80f2a8ef67349b38ad2ededabbb8d",
                    "Gateway": "172.23.0.1",
                    "IPAddress": "172.23.0.2",
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "MacAddress": "02:42:ac:17:00:02",
                    "DriverOpts": null
                }
}
```

然后把哨兵的docker-compose.yml下的networks:下的name为_default

修改sentinel相关config文件里面的mater ip为172.23.0.2





查看主库、从库信息

```
>redis-cli -p 6379 -a 1234
>info
```

查看26379端口的sentinel信息·1

```
>redis-cli -p 26379
>info
```

