#!/bin/bash
# 忠济堂服务守护脚本 - 快速检测并重启停止的服务
# 由cron每5分钟调用，或手动执行

LOG="/tmp/watchdog.log"
timestamp() { date '+%Y-%m-%d %H:%M:%S'; }

log() { echo "[$(timestamp)] $1" >> "$LOG"; }

restart_mysql() {
  if ! pgrep -x mysqld > /dev/null; then
    log "MySQL停止，重启中..."
    mkdir -p /var/run/mysqld && chown mysql:mysql /var/run/mysqld
    mysqld_safe --user=mysql --port=3306 --bind-address=0.0.0.0 &
    for i in $(seq 1 15); do
      mysqladmin ping -h 127.0.0.1 --silent 2>/dev/null && break
      sleep 2
    done
    log "MySQL重启完成"
  fi
}

restart_redis() {
  if ! pgrep -x redis-server > /dev/null; then
    log "Redis停止，重启中..."
    redis-server --daemonize yes --port 6379 2>/dev/null
    log "Redis重启完成"
  fi
}

restart_backend() {
  if ! curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/v1/user/employees/login 2>/dev/null | grep -q '200\|405'; then
    if ! pgrep -f 'zjt-app-1.0.0-SNAPSHOT.jar' > /dev/null; then
      log "后端停止，重启中..."
      if [ -f /workspace/backend/zjt-app/target/zjt-app-1.0.0-SNAPSHOT.jar ]; then
        cd /workspace/backend && env -u HTTP_PROXY -u HTTPS_PROXY nohup java -jar zjt-app/target/zjt-app-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev > /tmp/backend.log 2>&1 &
        log "后端启动中，等待就绪..."
        for i in $(seq 1 30); do
          curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/v1/user/employees/login 2>/dev/null | grep -q '200\|405' && break
          sleep 3
        done
        log "后端重启完成"
      else
        log "JAR包不存在，跳过后端重启"
      fi
    fi
  fi
}

restart_frontend() {
  local name=$1 dir=$2 port=$3
  if ! curl -s -o /dev/null -w '%{http_code}' http://localhost:$port/ 2>/dev/null | grep -q '200'; then
    log "$name停止，重启中..."
    if [ -d "$dir" ]; then
      cd "$dir"
      [ ! -d node_modules ] && npm install 2>/dev/null
      nohup npx vite --host 0.0.0.0 --port $port > "/tmp/frontend-$name.log" 2>&1 &
      log "$name重启完成"
    fi
  fi
}

restart_nginx() {
  if ! pgrep -x nginx > /dev/null; then
    log "Nginx停止，重启中..."
    # 确保配置存在
    if [ ! -f /etc/nginx/sites-available/zjt ]; then
      cat > /etc/nginx/sites-available/zjt << 'NGINX'
server {
    listen 3000;
    server_name _;
    location = / { return 302 /admin/; }
    location /api/ { proxy_pass http://127.0.0.1:8080/api/; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; proxy_read_timeout 300s; }
    location /admin/ { proxy_pass http://127.0.0.1:3001/admin/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /h5/ { proxy_pass http://127.0.0.1:3002/h5/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /reception/ { proxy_pass http://127.0.0.1:3003/reception/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /manager/ { proxy_pass http://127.0.0.1:3004/manager/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
    location /therapist/ { proxy_pass http://127.0.0.1:3005/therapist/; proxy_set_header Host $host; proxy_set_header Upgrade $http_upgrade; proxy_set_header Connection "upgrade"; }
}
NGINX
      ln -sf /etc/nginx/sites-available/zjt /etc/nginx/sites-enabled/zjt 2>/dev/null
      rm -f /etc/nginx/sites-enabled/default 2>/dev/null
    fi
    nginx 2>/dev/null
    log "Nginx重启完成"
  fi
}

check_db() {
  if ! mysql -u zjt -pzjt2024pwd -h 127.0.0.1 -e "USE zjt_dev" 2>/dev/null; then
    log "数据库不存在，初始化中..."
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
    mysql -u zjt -pzjt2024pwd zjt_dev -e "ALTER TABLE store_schedule ADD COLUMN IF NOT EXISTS shift_type VARCHAR(20) DEFAULT 'morning';" 2>/dev/null
    log "数据库初始化完成"
  fi
}

# ============ 主流程 ============
log "===== 守护检查开始 ====="

# 0. 确保基础软件已安装
if ! which mysqld &>/dev/null; then
  log "MySQL未安装，安装中..."
  export DEBIAN_FRONTEND=noninteractive
  apt-get update -qq 2>/dev/null
  echo "mysql-server mysql-server/root_password password root" | debconf-set-selections 2>/dev/null
  echo "mysql-server mysql-server/root_password_again password root" | debconf-set-selections 2>/dev/null
  apt-get install -y -qq mysql-server redis-server nginx 2>/dev/null
  log "基础软件安装完成"
fi

restart_mysql
check_db
restart_redis
restart_backend
restart_frontend "admin" "/workspace/frontend/zjt-admin" 3001
restart_frontend "h5" "/workspace/frontend/zjt-h5" 3002
restart_frontend "reception" "/workspace/frontend/zjt-reception" 3003
restart_frontend "manager" "/workspace/frontend/zjt-manager" 3004
restart_frontend "therapist" "/workspace/frontend/zjt-therapist" 3005
restart_nginx

log "===== 守护检查完成 ====="
