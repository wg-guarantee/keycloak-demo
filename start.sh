#!/bin/bash

# Docker Compose 启动脚本

set -e

PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "======================================"
echo "Keycloak Demo - Docker Compose 启动"
echo "======================================"

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "错误: 未找到 Docker，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "错误: 未找到 Docker Compose，请先安装 Docker Compose"
    exit 1
fi

# 检查环境变量文件
if [ ! -f "$PROJECT_DIR/.env" ]; then
    echo "警告: .env 文件不存在，创建默认配置..."
    cp "$PROJECT_DIR/.env.example" "$PROJECT_DIR/.env"
    echo "请编辑 .env 文件配置阿里云凭证，然后重新运行此脚本"
    exit 1
fi

echo "启动 Docker Compose 服务..."
cd "$PROJECT_DIR"
docker-compose up -d

echo ""
echo "======================================"
echo "服务启动中... 请稍候 30 秒"
echo "======================================"
sleep 30

# 检查服务状态
echo ""
echo "检查服务状态..."
docker-compose ps

echo ""
echo "======================================"
echo "启动完成！"
echo "======================================"
echo ""
echo "访问地址:"
echo "  - Keycloak 管理界面: http://localhost:8180"
echo "    用户名: admin"
echo "    密码: admin123"
echo ""
echo "  - Spring Boot API: http://localhost:8080/api"
echo ""
echo "查看日志:"
echo "  docker-compose logs -f"
echo ""
echo "停止服务:"
echo "  docker-compose down"
echo ""
