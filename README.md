# 一、gRPC端口转发
如果没有为 Temporal Frontend 服务指定 Ingress，则可以通过端口转发进行访问。
```shell
# 端口转发至 Temporal Frontend 服务gRPC端口
kubectl port-forward svc/test-temporal-frontend -n temporal 7233:7233
```
# 二、Temporal Namespace 注册
worker启动之前检查命名空间是否已经注册，如果没有则注册。
```shell
# 在 temporal 命名空间下的 admintools 容器里执行注册命令
tctl --ns default namespace register
```