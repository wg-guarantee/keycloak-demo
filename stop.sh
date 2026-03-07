#!/bin/bash

# Docker Compose 停止脚本

set -e

PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "======================================"
echo "Keycloak Demo - Docker Compose 停止"
echo "======================================"

cd "$PROJECT_DIR"

# 询问是否删除数据
read -p "是否删除所有数据（数据库、卷等）? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "停止并删除所有容器和数据..."
    docker-compose down -v
    echo "所有数据已删除"
else
    echo "停止容器（保留数据）..."
    docker-compose down
    echo "容器已停止，数据保留"
fi

echo "完成！"
