#!/bin/bash

# 测试脚本 - 验证所有功能

BASE_URL="http://localhost:8080/api"
KEYCLOAK_URL="http://localhost:8180"

echo "======================================"
echo "Keycloak Demo - 功能测试"
echo "======================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -n "测试 $description... "
    
    if [ "POST" = "$method" ]; then
        if [ -z "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint")
        else
            response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    else
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -lt 400 ]; then
        echo -e "${GREEN}✓ ($http_code)${NC}"
        return 0
    else
        echo -e "${RED}✗ ($http_code)${NC}"
        echo "  响应: $body"
        return 1
    fi
}

# 1. 健康检查
echo "1. 健康检查"
test_endpoint GET "/health" "" "应用健康状态"
test_endpoint GET "" "" "Keycloak" 2>/dev/null || true

echo ""
echo "2. 认证功能"

# 生成唯一用户名
TIMESTAMP=$(date +%s)
TEST_USERNAME="testuser_$TIMESTAMP"
TEST_EMAIL="test_$TIMESTAMP@example.com"

# 注册
test_endpoint POST "/auth/register" \
    "{\"username\":\"$TEST_USERNAME\",\"email\":\"$TEST_EMAIL\",\"password\":\"Password123!\",\"phone\":\"+8613800000000\"}" \
    "用户注册"

# 发送短信
test_endpoint POST "/auth/send-sms" \
    "{\"phone\":\"+8613800000000\",\"type\":\"VERIFICATION\"}" \
    "发送短信验证码"

echo ""
echo "======================================"
echo "测试完成！"
echo "======================================"
echo ""
echo "更多功能验证请参考 docs/SETUP.md"
echo ""
