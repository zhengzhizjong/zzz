#!/bin/bash
# 忠济堂服务守护进程 - 持续运行，每60秒检查一次服务状态
# 启动方式: nohup bash /workspace/daemon.sh > /tmp/daemon.log 2>&1 &

INTERVAL=60  # 检查间隔（秒）

while true; do
  # MySQL
  if ! pgrep -x mysqld > /dev/null; then
    echo "[$(date '+%H:%M:%S')] MySQL停止，重启..." >> /tmp/daemon.log
    mkdir -p /var/run/mysqld && chown mysql:mysql /var/run/mysqld
    mysqld_safe --user=mysql --port=3306 --bind-address=0.0.0.0 &
    sleep 10
  fi

  # Redis
  if ! pgrep -x redis-server > /dev/null; then
    echo "[$(date '+%H:%M:%S')] Redis停止，重启..." >> /tmp/daemon.log
    redis-server --daemonize yes --port 6379 2>/dev/null
  fi

  # 后端
  if ! curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/v1/user/employees/login 2>/dev/null | grep -q '200\|405'; then
    if ! pgrep -f 'zjt-app-1.0.0-SNAPSHOT.jar' > /dev/null; then
      echo "[$(date '+%H:%M:%S')] 后端停止，重启..." >> /tmp/daemon.log
      if [ -f /workspace/backend/zjt-app/target/zjt-app-1.0.0-SNAPSHOT.jar ]; then
        cd /workspace/backend && env -u HTTP_PROXY -u HTTPS_PROXY nohup java -jar zjt-app/target/zjt-app-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev > /tmp/backend.log 2>&1 &
      fi
    fi
  fi

  # 前端服务
  for app_info in "admin:/workspace/frontend/zjt-admin:3001" "h5:/workspace/frontend/zjt-h5:3002" "reception:/workspace/frontend/zjt-reception:3003" "manager:/workspace/frontend/zjt-manager:3004" "therapist:/workspace/frontend/zjt-therapist:3005"; do
    name="${app_info%%:*}"
    rest="${app_info#*:}"
    dir="${rest%:*}"
    port="${rest##*:}"
    if ! curl -s -o /dev/null -w '%{http_code}' http://localhost:$port/ 2>/dev/null | grep -q '200'; then
      echo "[$(date '+%H:%M:%S')] $name停止，重启..." >> /tmp/daemon.log
      cd "$dir" && nohup npx vite --host 0.0.0.0 --port $port > "/tmp/frontend-$name.log" 2>&1 &
    fi
  done

  # Nginx
  if ! pgrep -x nginx > /dev/null; then
    echo "[$(date '+%H:%M:%S')] Nginx停止，重启..." >> /tmp/daemon.log
    nginx 2>/dev/null
  fi

  sleep $INTERVAL
done
