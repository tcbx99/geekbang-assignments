# 作业说明

本作业包含一份压测报告，并附有压测使用的环境信息、Java工程、数据库表结构及数据等一切内容。

TL;DR: 这个文档不是压测报告，去看 `REPORT.md`

## 目录说明

| 目录 | 用途 |
|----|----|
| demo-project | 压测使用的 Java 工程 |
| scripts | 压测使用的脚本 |
| plans | 压测使用的 JMeter 工程 |
| REPORT.md | 压测报告 |
| README.md | 本文档 |

## 压测环境

- 云平台：阿里云，北京 可用区G
- 实例类型：ecs.c5.xlarge, 4 vCPU 8 GiB IO优化
- 网络：专有网络，自建VPC与交换机；IP 192.168.33.0/24

设备如表所示：

| IP | OS | 用途 |
|---|---|---|
| 192.168.33.11 | AlmaLinux 9.0 | 监控平台 |
| 192.168.33.12 | AlmaLinux 9.0 | PostgreSQL 数据库 |
| 192.168.33.13 | AlmaLinux 9.0 | Java 工程 |
| 192.168.33.21 | Windows Server 2022 | 压测机 |

安装的软件如下：

- 所有 Linux 设备:
    - node_exporter
- 192.168.33.11:
    - podman
    - grafana:latest (podman)
    - prometheus:v2.15.1 (podman)
    - influxdb:1.8 (podman)
- 192.168.33.12:
    - podman
    - postgresql:14 (podman)
- 192.168.33.13:
    - java-1.8.0-openjdk-devel
    - app.jar (应用程序)
    - postgres (使用了 `psql` 客户端导入 DDL 补丁)
- 192.168.33.21:
    - JMeter

## 使用

### 第一步：构建工程

```bash
cd demo-project
./gradlew bootJar
```

获得 `demo-project/build/libs/geek-demo-for-stress-0.0.1-SNAPSHOT.jar`，重命名为 `app.jar`。

### 第二步：构建环境

**注意: 本实验使用的阿里云OSS Endpoint已被抹去，请自行构建存储桶，赋予权限并上传对应文件**

在所有设备上运行脚本，安装 `node_exporter`

```bash
#!/bin/bash
mkdir /tmp/gk-node_exporter
cd /tmp/gk-node_exporter
wget https://${ENDPOINT}/node_exporter-0.18.1.linux-amd64.tar.gz
tar xvf node_exporter-0.18.1.linux-amd64.tar.gz
mv node_exporter-0.18.1.linux-amd64/node_exporter /usr/local/bin
cd /root
rm -rf /tmp/gk-node_exporter
file /usr/local/bin/node_exporter
cat >/etc/systemd/system/node_exporter.service <<EOF
[Unit]
Description=node_exporter
After=multi-user.target
Wants=network-online.target

[Service]
Type=simple
ExecStart=/usr/local/bin/node_exporter

[Install]
WantedBy=multi-user.target
EOF
systemctl enable --now node_exporter
```

在第一台设备上运行脚本，安装压测平台

```bash
#!/bin/bash
dnf install -y podman
systemctl enable --now podman-restart.service
# Install InfluxDB
podman run -d --name influxdb -p 8083:8083 -p 8086:8086 --restart always docker.io/library/influxdb:1.8
# Create Prometheus configuration
mkdir -p /opt/geektime/prometheus/
cat <<EOF > /opt/geektime/prometheus/prometheus.yml
# my global config
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      # - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: hero-linux
    static_configs:
      - targets:
          - "192.168.33.11:9100"
          - "192.168.33.12:9100"
          - "192.168.33.13:9100"
EOF
chmod -R 755 /opt/geektime
# Install Prometeus
podman run -d --name prometheus -p 9091:9090 -v /opt/geektime/prometheus:/etc/prometheus:rw --restart always docker.io/prom/prometheus:v2.15.1
# Install Grafana
podman run -d --name grafana -p 3000:3000 --restart always docker.io/grafana/grafana:latest
```

在数据库机安装PG

```bash
#!/bin/bash
# Files
mkdir /tmp/geektime
cd /tmp/geektime
wget https://${ENDPOINT}/app/users.sql.xz
xz -d users.sql.xz
# Install podman
dnf install -y podman
systemctl enable --now podman-restart.service
podman run -d --restart always --name postgres \
    -e POSTGRES_PASSWORD=password -e ALLOW_IP_RANGE=192.168.33.0/24 \
    -p 5432:5432 docker.io/library/postgres:14
# Wait
sleep 5
# Initialize Database
podman cp users.sql postgres:/tmp/users.sql
podman exec -u postgres postgres /bin/bash -c "psql -U postgres -f /tmp/users.sql | wc"
podman exec postgres /bin/bash -c "rm -f /tmp/users.sql"
# Done!
echo "Happy Coding! :-)"
```

在应用机安装应用（不包含启动脚本）

```bash
#!/bin/bash
# Install Java and PostgreSQL client
dnf install -y java-1.8.0-openjdk-devel postgresql
# Prepare /opt/geektime
mkdir /opt/geektime
cd /opt/geektime
wget https://${ENDPOINT}/app/app.jar
wget https://${ENDPOINT}/app/update-index.sql
# You should write your own boot script here
echo "TODO: Write your boot script in ${pwd}"
chown -R ecs-user:ecs-user /opt/geektime
echo "Happy Coding! :-)"
```

SSH进应用服务器，执行以下脚本启动应用

```bash
#!/bin/bash

JAVA_OPTS="$JAVA_OPTS -Xmx512M -Xms512M"

nohup java -jar $JAVA_OPTS -jar app.jar > app.log &
```

执行 `curl http://192.168.33.13/api/v1/users?keyword=A&size=1&page=0` 有结果，即安装完成

## 压力测试相关

请看压测报告