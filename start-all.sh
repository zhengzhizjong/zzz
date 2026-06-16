#!/bin/bash
# 忠济堂全栈服务一键启动脚本（含环境初始化）
# 用法: bash /workspace/start-all.sh
# 首次运行会安装MySQL/Redis并初始化数据库，后续运行只启动服务

echo "=========================================="
echo "  忠济堂中医养生连锁管理系统 - 启动所有服务"
echo "=========================================="

# ============ 0. 环境初始化（仅首次需要） ============
if ! id mysql &>/dev/null || ! which mysqld &>/dev/null; then
  echo "[0/7] 安装基础环境（MySQL + Redis + Nginx）..."
  export DEBIAN_FRONTEND=noninteractive
  apt-get update -qq
  echo "mysql-server mysql-server/root_password password root" | debconf-set-selections
  echo "mysql-server mysql-server/root_password_again password root" | debconf-set-selections
  apt-get install -y -qq mysql-server redis-server nginx 2>/dev/null
  echo "  安装完成"
fi

# ============ 1. 启动 MySQL ============
echo "[1/7] 启动 MySQL..."
if pgrep -x mysqld > /dev/null; then
  echo "  MySQL 已在运行"
else
  mkdir -p /var/run/mysqld && chown mysql:mysql /var/run/mysqld
  mysqld_safe --user=mysql --port=3306 --bind-address=0.0.0.0 &
  for i in $(seq 1 30); do
    if mysqladmin ping -h 127.0.0.1 --silent 2>/dev/null; then
      echo "  MySQL 启动成功"
      break
    fi
    sleep 2
  done
fi

# ============ 2. 初始化数据库（如果不存在） ============
echo "[2/7] 检查数据库..."
if ! mysql -u zjt -pzjt2024pwd -h 127.0.0.1 -e "USE zjt_dev" 2>/dev/null; then
  echo "  数据库不存在，开始初始化..."
  DEBIAN_MAINT_PASS=$(grep -m1 'password' /etc/mysql/debian.cnf 2>/dev/null | awk '{print $3}')
  mysql -u debian-sys-maint -p"$DEBIAN_MAINT_PASS" -e "
    CREATE DATABASE IF NOT EXISTS zjt_dev DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE USER IF NOT EXISTS 'zjt'@'%' IDENTIFIED WITH mysql_native_password BY 'zjt2024pwd';
    CREATE USER IF NOT EXISTS 'zjt'@'localhost' IDENTIFIED WITH mysql_native_password BY 'zjt2024pwd';
    GRANT ALL PRIVILEGES ON zjt_dev.* TO 'zjt'@'%';
    GRANT ALL PRIVILEGES ON zjt_dev.* TO 'zjt'@'localhost';
    FLUSH PRIVILEGES;
  " 2>/dev/null
  mysql -u zjt -pzjt2024pwd zjt_dev < /workspace/backend/sql/V1.0.0__init_all_tables.sql 2>/dev/null
  mysql -u zjt -pzjt2024pwd zjt_dev < /workspace/backend/sql/V3.0.0__realistic_store_data.sql 2>/dev/null
  mysql -u zjt -pzjt2024pwd zjt_dev -e "
    ALTER TABLE store_schedule ADD COLUMN IF NOT EXISTS shift_type VARCHAR(20) DEFAULT 'morning' COMMENT '班次:morning早班,afternoon中班,evening晚班';
  " 2>/dev/null
  echo "  数据库初始化完成"
else
  echo "  数据库已就绪"
fi

# ============ 3. 启动 Redis ============
echo "[3/7] 启动 Redis..."
if pgrep -x redis-server > /dev/null; then
  echo "  Redis 已在运行"
else
  redis-server --daemonize yes --port 6379 2>/dev/null || redis-server --port 6379 &
  sleep 2
  echo "  Redis 启动成功"
fi

# ============ 4. 构建后端（如果JAR不存在） ============
echo "[4/7] 检查后端JAR包..."
if [ ! -f /workspace/backend/zjt-app/target/zjt-app-1.0.0-SNAPSHOT.jar ]; then
  echo "  JAR包不存在，开始构建..."
  cd /workspace/backend
  mkdir -p ~/.m2
  cat > ~/.m2/settings.xml << 'MAVEN_SETTINGS'
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    </mirror>
  </mirrors>
  <proxies>
    <proxy>
      <id>http-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>127.0.0.1</host>
      <port>18080</port>
      <nonProxyHosts>localhost|127.0.0.1|*.aliyun.com</nonProxyHosts>
    </proxy>
  </proxies>
</settings>
MAVEN_SETTINGS
  # 先安装父POM
  mvn clean install -DskipTests -N -q 2>&1 | tail -2
  # 再构建全部
  mvn clean package -pl zjt-app -am -DskipTests -q 2>&1 | tail -2
  echo "  构建完成"
else
  echo "  JAR包已存在"
fi

# ============ 5. 启动后端 Spring Boot ============
echo "[5/7] 启动后端服务..."
if curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/v1/user/employees/login 2>/dev/null | grep -q '200\|405'; then
  echo "  后端已在运行"
else
  cd /workspace/backend
  env -u HTTP_PROXY -u HTTPS_PROXY nohup java -jar zjt-app/target/zjt-app-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=dev > /tmp/backend.log 2>&1 &
  echo "  后端启动中，等待就绪..."
  for i in $(seq 1 60); do
    if curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/v1/user/employees/login 2>/dev/null | grep -q '200\|405'; then
      echo "  后端启动成功 (耗时约$((i*2))秒)"
      break
    fi
    sleep 2
  done
fi

# ============ 6. 启动前端 Vite Dev Servers ============
echo "[6/7] 启动前端服务..."

start_frontend() {
  local name=$1
  local dir=$2
  local port=$3

  if curl -s -o /dev/null -w '%{http_code}' http://localhost:$port/ 2>/dev/null | grep -q '200'; then
    echo "  $name 已在运行 (端口$port)"
    return
  fi

  if [ ! -d "$dir/node_modules" ]; then
    echo "  安装 $name 依赖..."
    cd "$dir" && npm install 2>/dev/null
  fi

  cd "$dir"
  nohup npx vite --host 0.0.0.0 --port $port > "/tmp/frontend-${name}.log" 2>&1 &
  echo "  $name 启动中 (端口$port)..."
}

start_frontend "admin" "/workspace/frontend/zjt-admin" 3001
start_frontend "h5" "/workspace/frontend/zjt-h5" 3002
start_frontend "reception" "/workspace/frontend/zjt-reception" 3003
start_frontend "manager" "/workspace/frontend/zjt-manager" 3004
start_frontend "therapist" "/workspace/frontend/zjt-therapist" 3005

echo "  等待前端服务就绪..."
sleep 10

# 启动 Nginx（如果未运行）
if ! pgrep -x nginx > /dev/null; then
  # 确保nginx配置存在
  if [ ! -f /etc/nginx/sites-available/zjt ]; then
    cat > /etc/nginx/sites-available/zjt << 'NGINX_CONF'
server {
    listen 3000;
    server_name _;
    location = / { return 302 /admin/; }
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 300s;
        proxy_connect_timeout 300s;
    }
    location /admin/ { proxy_pass http://127.0.0.1:3001/admin/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /h5/ { proxy_pass http://127.0.0.1:3002/h5/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /reception/ { proxy_pass http://127.0.0.1:3003/reception/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /manager/ { proxy_pass http://127.0.0.1:3004/manager/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /therapist/ { proxy_pass http://127.0.0.1:3005/therapist/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
}
NGINX_CONF
    ln -sf /etc/nginx/sites-available/zjt /etc/nginx/sites-enabled/zjt
    rm -f /etc/nginx/sites-enabled/default
  fi
  nginx 2>/dev/null
fi

# ============ 7. 健康检查 ============
echo "[7/7] 健康检查..."
echo ""

check_service() {
  local name=$1
  local url=$2
  local code=$(curl -s -o /dev/null -w '%{http_code}' "$url" 2>/dev/null)
  if [ "$code" = "200" ] || [ "$code" = "302" ]; then
    printf "  %-12s 正常 (HTTP %s)\n" "$name" "$code"
  else
    printf "  %-12s 异常 (HTTP %s)\n" "$name" "$code"
  fi
}

check_service "后端API" "http://localhost:8080/api/v1/user/employees/login"
check_service "Admin" "http://localhost:3001/admin/"
check_service "H5客户端" "http://localhost:3002/h5/"
check_service "前台端" "http://localhost:3003/reception/"
check_service "店长端" "http://localhost:3004/manager/"
check_service "技师端" "http://localhost:3005/therapist/"
check_service "Nginx" "http://localhost:3000/"

echo ""
echo "=========================================="
echo "  访问地址:"
echo "  统一入口: http://localhost:3000/"
echo "  管理后台: http://localhost:3000/admin/"
echo "  H5客户端: http://localhost:3000/h5/"
echo "  前台端:   http://localhost:3000/reception/"
echo "  店长端:   http://localhost:3000/manager/"
echo "  技师端:   http://localhost:3000/therapist/"
echo "=========================================="
echo ""
echo "提示: 如果服务停止，运行 bash /workspace/start-all.sh 即可恢复"
