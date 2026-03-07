# Keycloak Demo 项目 Makefile

.PHONY: help up down logs clean restart build test dev-setup

# 默认目标
.DEFAULT_GOAL := help

# 获取项目目录
PROJECT_DIR := $(shell pwd)

help:
	@echo "Keycloak Demo - Makefile 命令"
	@echo "======================================"
	@echo "命令列表:"
	@echo "  make up          - 启动所有服务"
	@echo "  make down        - 停止所有服务（保留数据）"
	@echo "  make clean       - 停止并删除所有数据"
	@echo "  make logs        - 查看实时日志"
	@echo "  make logs-app    - 查看应用日志"
	@echo "  make logs-db     - 查看数据库日志"
	@echo "  make logs-kc     - 查看 Keycloak 日志"
	@echo "  make restart     - 重启所有服务"
	@echo "  make build       - 构建 Spring Boot 镜像"
	@echo "  make test        - 运行功能测试"
	@echo "  make dev-setup   - 本地开发环境设置"
	@echo "  make ps          - 显示服务状态"
	@echo "  make shell-db    - 进入 MySQL 容器"
	@echo "  make shell-app   - 进入应用容器"

up:
	@echo "启动 Docker Compose 服务..."
	docker-compose up -d
	@echo "等待服务启动..."
	@sleep 10
	@echo "服务已启动，请访问:"
	@echo "  Keycloak: http://localhost:8180"
	@echo "  API: http://localhost:8080/api"

down:
	@echo "停止 Docker Compose 服务..."
	docker-compose down

clean:
	@echo "停止服务并删除所有数据..."
	docker-compose down -v
	@echo "清理完成！"

restart:
	@$(MAKE) down
	@$(MAKE) up

logs:
	docker-compose logs -f

logs-app:
	docker-compose logs -f spring-boot-app

logs-db:
	docker-compose logs -f mysql

logs-kc:
	docker-compose logs -f keycloak

build:
	@echo "构建 Spring Boot 镜像..."
	docker-compose build spring-boot-app

test:
	@echo "运行功能测试..."
	@bash test.sh

dev-setup:
	@echo "初始化本地开发环境..."
	@[ -f .env ] || (echo "创建 .env 文件..." && cp .env.example .env)
	@echo "环境设置完成！"
	@echo "请编辑 .env 文件配置阿里云凭证，然后运行 'make up'"

ps:
	docker-compose ps

shell-db:
	docker exec -it keycloak-demo-mysql mysql -u${MYSQL_USER:-webank} -p${MYSQL_PASSWORD:-webank123} ${MYSQL_DATABASE:-webank}

shell-app:
	docker exec -it keycloak-demo-app /bin/bash

migrate:
	@echo "强制执行数据库迁移..."
	docker-compose down
	docker volume rm keycloak-demo_mysql_data keycloak-demo_keycloak_data 2>/dev/null || true
	docker-compose up -d

validate-env:
	@echo "验证环境变量..."
	@[ -f .env ] && echo "✓ .env 文件存在" || (echo "✗ .env 文件不存在" && exit 1)
	@grep -q 'ALIYUN_ACCESS_KEY_ID' .env && echo "✓ 阿里云 KEY 已配置" || echo "⚠ 阿里云 KEY 未配置"
	@echo "环境验证完成！"
